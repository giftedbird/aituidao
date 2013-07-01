package com.sound.dubbler.model.sound_player;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;

import com.plugin.common.utils.DebugLog;
import com.plugin.common.utils.Destroyable;
import com.plugin.common.utils.NotifyHandlerObserver;
import com.plugin.common.utils.SingleInstanceBase;
import com.plugin.common.utils.files.DiskManager;
import com.plugin.common.utils.files.DiskManager.DiskCacheType;

import com.sound.dubbler.R;
import com.sound.dubbler.utils.ProximitySensor;
import com.sound.dubbler.utils.ProximitySensor.ModeChangeListener;

/**
 * @author Yuanzhe Guo 2013-3-14上午10:16:06
 */
public class SoundPlayer extends SingleInstanceBase implements Destroyable {
	public static interface Playable {
		public long getSoundDuration();
	}
	
	public static interface Mp3UriPlayable extends Playable {
		public URI getMp3SoundUri();
	}
	
	public static interface PcmDataPlayable extends Playable {
		public BlockingQueue<PcmData> getPcmDataQueue();
	}
	
	public static final int PCM_DATA_QUEUE_DEFAULT_SIZE = 200;
	
	public static final int DATA_LENGTH_FINISHED = -1;
	public static final int DATA_LENGTH_ERROR = -2;
	
	public static final class PcmData {
		public int mSampleRateInHz;
		public boolean mIsMono;
		public short[] mData;
		public int mLength;
	}
	
	public static final int CB_NEW = R.id.sound_player_new;
	public static final int CB_LOADING = R.id.sound_player_loading;
	public static final int CB_START = R.id.sound_player_start;
	public static final int CB_STOP = R.id.sound_player_stop;
	public static final int CB_FINISH = R.id.sound_player_finish;
	public static final int CB_ERROR = R.id.sound_player_error;
	public static final int CB_PROGRESS = R.id.sound_player_progress;
	
	public static enum State {
		LOADING,
		PLAYING,
		FINISHED,
	}
	
	private static final String TAG = "[[SoundPlayer]]";
	
	private static final int MIN_DURATION_TO_REPLAY = 5000;
	
	private static final int WHAT_ERROR = 0;
	private static final int WHAT_FINISHED = 1;
	private static final int WHAT_START = 2;
	private static final int WHAT_LOADING = 3;
	private static final int WHAT_PROGRESS_PCM = 4;
	
	private static final int PROGRESS_PERIOD_MS = 50;
	
	private static final int MIN_BUFFER_TIME_MS_TO_PLAY = 3 * 1000;
	
	private static final int DELAY_FINISHED_MS = 150;
	
	private static final int MP3_DOWNLOAD_BUFFER_SIZE = 512;
	
	// 下面这个值一定至少要大于一个mp3帧的最大值
	private static final int MP3_STREAM_BUFFER_SIZE = 100 * 1024;
	
	private NotifyHandlerObserver newNotifyHandlerListener = new NotifyHandlerObserver(CB_NEW);
	private NotifyHandlerObserver loadingNotifyHandlerListener = new NotifyHandlerObserver(CB_LOADING);
	private NotifyHandlerObserver startNotifyHandlerListener = new NotifyHandlerObserver(CB_START);
	private NotifyHandlerObserver stopNotifyHandlerListener = new NotifyHandlerObserver(CB_STOP);
	private NotifyHandlerObserver finishNotifyHandlerListener = new NotifyHandlerObserver(CB_FINISH);
	private NotifyHandlerObserver errorNotifyHandlerListener = new NotifyHandlerObserver(CB_ERROR);
	private NotifyHandlerObserver progressNotifyHandlerListener = new NotifyHandlerObserver(CB_PROGRESS);
	
	private Playable mPlayable = null;
	private State mState = State.FINISHED;
	private int mPlayingTime = 0;
	private ProximitySensor mInCallManager = null;
	private Context mContext;
	private PowerManager.WakeLock mWakeLock = null;
	private boolean mIsWakeLock = false;
	
	private ExecutorService mDownloadESForLocal = Executors.newSingleThreadExecutor();
	private ExecutorService mDownloadESForNetwork = Executors.newSingleThreadExecutor();
	@SuppressWarnings("rawtypes")
	private Future mDownloadFuture = null;
	
	private ExecutorService mDecodeESForLocal = Executors.newSingleThreadExecutor();
	private ExecutorService mDecodeESForNetwork = Executors.newSingleThreadExecutor();
	@SuppressWarnings("rawtypes")
	private Future mDecodeFuture = null;
	
	private ExecutorService mPlayES = Executors.newSingleThreadExecutor();
	@SuppressWarnings("rawtypes")
	private Future mPlayFuture = null;
	
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_ERROR:
				if (mPlayable == msg.obj) {
					errorNotifyHandlerListener.notifyAll(-1, -1, msg.obj);
					
					stopInternal(false, true);
				}
				break;
				
			case WHAT_FINISHED:
				if (mPlayable == msg.obj) {
					finishNotifyHandlerListener.notifyAll(-1, -1, msg.obj);
					
					stopInternal(false, true);
				}
				break;
				
			case WHAT_START:
				if (mPlayable == msg.obj) {
					if (mState != State.PLAYING) {
						mState = State.PLAYING;
						startNotifyHandlerListener.notifyAll(-1, -1, msg.obj);
					}
				}
				break;
				
			case WHAT_LOADING:
				if (mPlayable == msg.obj) {
					if (mState != State.LOADING) {
						mState = State.LOADING;
						loadingNotifyHandlerListener.notifyAll(mPlayingTime, 0, msg.obj);
					}
				}
				break;
				
			case WHAT_PROGRESS_PCM:
				if (mPlayable == msg.obj) {
					if (mState == State.PLAYING) {
						mPlayingTime = msg.arg1;
						progressNotifyHandlerListener.notifyAll(msg.arg1, 0, msg.obj);
					}
				}
			}
		}
	};
	
	protected SoundPlayer() {
		super();
	}
	
	@Override
    protected void init(Context context) {
		mContext = context.getApplicationContext();
		mWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
						.newWakeLock(
								PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
								TAG);
    }
	
	@Override
    public void onDestroy() {
    	stop();
		
		newNotifyHandlerListener.removeAllObserver();
		loadingNotifyHandlerListener.removeAllObserver();
		startNotifyHandlerListener.removeAllObserver();
		stopNotifyHandlerListener.removeAllObserver();
		finishNotifyHandlerListener.removeAllObserver();
		errorNotifyHandlerListener.removeAllObserver();
		progressNotifyHandlerListener.removeAllObserver();
    }
	
	public void registerStateChangedHandlerListener(Handler handler) {
		newNotifyHandlerListener.registeObserver(handler);
		loadingNotifyHandlerListener.registeObserver(handler);
		startNotifyHandlerListener.registeObserver(handler);
		stopNotifyHandlerListener.registeObserver(handler);
		finishNotifyHandlerListener.registeObserver(handler);
		errorNotifyHandlerListener.registeObserver(handler);
		progressNotifyHandlerListener.registeObserver(handler);
	}
	
	public void unRegisterStateChangedHandlerListener(Handler handler) {
		newNotifyHandlerListener.unRegisteObserver(handler);
		loadingNotifyHandlerListener.unRegisteObserver(handler);
		startNotifyHandlerListener.unRegisteObserver(handler);
		stopNotifyHandlerListener.unRegisteObserver(handler);
		finishNotifyHandlerListener.unRegisteObserver(handler);
		errorNotifyHandlerListener.unRegisteObserver(handler);
		progressNotifyHandlerListener.unRegisteObserver(handler);
	}
	
	public void play(Playable playable) {
		playInternal(playable, true, true);
	}
	
	public void stop() {
		stopInternal(true, true);
	}
	
	public State getState() {
		return mState;
	}
	
	public int getPlayingTime() {
		return mPlayingTime;
	}
	
	public boolean canLocalPlaying(Mp3UriPlayable playable) {
		if (playable == null) {
			return false;
		}
		
		URI uri = playable.getMp3SoundUri();
		if (uri == null) {
			return false;
		}
		
		String scheme = uri.getScheme();
		if ((TextUtils.isEmpty(scheme)) || (scheme.equals("file"))) {
			return true;
		} else if (scheme.equals("http")) {
			if (checkHasLocal(uri.toString())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public Playable getPlayable() {
		return mPlayable;
	}
	
	private void playInternal(Playable playable, boolean notify, boolean releaseSensor) {
		stopInternal(notify, releaseSensor);
		
		if (playable == null) {
			DebugLog.d(TAG, "[playInternal()] playable = null");
			return;
		}
		
		BlockingQueue<PcmData> outsidePcmDataQ = null; 
		if (playable instanceof Mp3UriPlayable) {
			URI uri = ((Mp3UriPlayable) playable).getMp3SoundUri();
			if ((uri == null) || (TextUtils.isEmpty(uri.toString()))) {
				DebugLog.d(TAG, "[playInternal()] uri is empty");
				return;
			}
			
			String scheme = uri.getScheme();
			if ((!TextUtils.isEmpty(scheme)) &&
				(!scheme.equals("file")) &&
				(!scheme.equals("http"))) {
				DebugLog.d(TAG, "[playInternal()] the uri is wrong: " + uri);
				return;
			}
		} else if (playable instanceof PcmDataPlayable) {
			outsidePcmDataQ = ((PcmDataPlayable) playable).getPcmDataQueue();
			if (outsidePcmDataQ == null) {
				DebugLog.d(TAG, "[playInternal()] the playable pcm queue is null");
				return;
			}
		} else {
			DebugLog.d(TAG, "[playInternal()] playable must be Mp3UriPlayable or PcmDataPlayable");
			return;
		}
		
		if (notify) {
			newNotifyHandlerListener.notifyAll(-1, -1, playable);
		}
		
		if (mInCallManager == null) {
			InCallModeChangeListener l = new InCallModeChangeListener();
			mInCallManager = new ProximitySensor(mContext, false, l);
			l.setInitMode(mInCallManager.getCurrentMode());
		}
		mInCallManager.resume();
		
		if (!mIsWakeLock) {
			mWakeLock.acquire();
			mIsWakeLock = true;
			
			DebugLog.d(TAG, "[WakeLock] acquire");
		}
		
		mPlayable = playable;
		mState = State.LOADING;
		mPlayingTime = 0;
		
		loadingNotifyHandlerListener.notifyAll(mPlayingTime, 0, mPlayable);
		
		if (playable instanceof Mp3UriPlayable) {
			PipedOutputStream mp3DataOutputStream = new PipedOutputStream();
			PipedInputStream mp3DataInputStream;
			try {
				mp3DataInputStream = new PipedInputStream(mp3DataOutputStream, MP3_STREAM_BUFFER_SIZE);
			} catch (IOException e) {
				// 不可能运行到这里
				throw new RuntimeException("PipedOutputStream is already connected???!!!");
			}
			BlockingQueue<PcmData> pcmDataQ
				= new LinkedBlockingQueue<PcmData>(PCM_DATA_QUEUE_DEFAULT_SIZE);
			
			if (canLocalPlaying((Mp3UriPlayable) mPlayable)) {
				mDownloadFuture = mDownloadESForLocal.submit(
						new DownloadRunnable((Mp3UriPlayable) playable, mp3DataOutputStream));
				mDecodeFuture = mDecodeESForLocal.submit(
						new DecodeRunnable(mp3DataInputStream, pcmDataQ));
			} else {
				mDownloadFuture = mDownloadESForNetwork.submit(
						new DownloadRunnable((Mp3UriPlayable) playable, mp3DataOutputStream));
				mDecodeFuture = mDecodeESForNetwork.submit(
						new DecodeRunnable(mp3DataInputStream, pcmDataQ));
			}
			mPlayFuture = mPlayES.submit(new PlayRunnable(playable, pcmDataQ));
		} else if (playable instanceof PcmDataPlayable) {
			mPlayFuture = mPlayES.submit(new PlayRunnable(playable, outsidePcmDataQ));
		}
	}
	
	private void stopInternal(boolean notify, boolean releaseSensor) {
		DebugLog.d(TAG, "[stopInternal()]:" + mPlayable);
		
		if (notify) {
			stopNotifyHandlerListener.notifyAll(-1, -1, mPlayable);
		}
		
		if (mDownloadFuture != null) {
			mDownloadFuture.cancel(true);
			mDownloadFuture = null;
		}
		
		if (mDecodeFuture != null) {
			mDecodeFuture.cancel(true);
			mDecodeFuture = null;
		}
		
		if (mPlayFuture != null) {
			mPlayFuture.cancel(true);
			mPlayFuture = null;
		}
		
		mPlayable = null;
		mState = State.FINISHED;
		mPlayingTime = 0;
		
		if ((releaseSensor) && (mInCallManager != null)) {
			mInCallManager.pause();
			mInCallManager.release();
			mInCallManager = null;
		}
		
		if (mIsWakeLock) {
			mWakeLock.release();
			mIsWakeLock = false;
			
			DebugLog.d(TAG, "[WakeLock] release");
		}
	}
	
	private boolean checkHasLocal(String url) {
		String path = getLocalPath(url);
		File file = new File(path);
		if ((file.exists()) && (file.canRead())) {
			return true;
		} else {
			return false;
		}
	}
	
	private String getTempLocalPath(String url) {
		return getLocalPath(url) + "_db_temp";
	}
	
	private String getLocalPath(String url) {
		String fileName = url.replaceAll("[:/.#]", "_");
		String rootDir = getRootDir();
		return rootDir + File.separatorChar + fileName;
	}
	
	private String getRootDir() {
		return DiskManager.tryToFetchCachePathByType(DiskCacheType.DOWNLOAD_AUDIO);
	}
	
	private class DownloadRunnable implements Runnable {
		private PipedOutputStream mOutputStream;
		private Mp3UriPlayable mCurrPlayable;
		
		private DownloadRunnable(Mp3UriPlayable playable, PipedOutputStream ouputStream) {
			mOutputStream = ouputStream;
			mCurrPlayable = playable;
		}
		
		@Override
		public void run() {
			try {
				URI uri = mCurrPlayable.getMp3SoundUri();
				String url = uri.toString();
				DebugLog.d(TAG, "[DownloadRunnable] enter:" + url);
				
				File dir = new File(getRootDir());
				dir.mkdirs();
				
				File tempFile;
				boolean totalLocalPlay;
				String scheme = uri.getScheme();
				if ((TextUtils.isEmpty(scheme)) || (scheme.equals("file"))) {
					totalLocalPlay = true;
					tempFile = new File(uri.getPath());
				} else if (canLocalPlaying(mCurrPlayable)) {
					totalLocalPlay = true;
					tempFile = new File(getLocalPath(url));
				} else {
					totalLocalPlay = false;
					tempFile = new File(getTempLocalPath(url));
				}
				
				long fileSize;
				if (tempFile.exists()) {
					fileSize = tempFile.length();
				} else {
					fileSize = 0;
				}
				DebugLog.d(TAG, "[DownloadRunnable] the exist file size is " + fileSize);
				
				if (fileSize > 0) {
					InputStream fis = null;
					try {
						fis = new BufferedInputStream(new FileInputStream(tempFile));
						while (true) {
							if (Thread.interrupted()) {
								DebugLog.d(TAG, "[DownloadRunnable] exit because interrupt");
								return;
							}
							
							byte[] buffer = new byte[MP3_DOWNLOAD_BUFFER_SIZE];
							int num = fis.read(buffer);
							if (num < 0) {
								break;
							} else {
								mOutputStream.write(buffer, 0, num);
							}
						}
					} catch (IOException e) {
						DebugLog.d(TAG, "[DownloadRunnable] the exist file read error or write output error");
						return;
					} finally {
						if (fis != null) {
							try {
								fis.close();
							} catch (IOException e) {
							}
						}
					}
				}
				
				if (totalLocalPlay) {
					DebugLog.d(TAG, "[DownloadRunnable] exit because local play");
					return;
				}
				
				if (Thread.interrupted()) {
					DebugLog.d(TAG, "[DownloadRunnable] exit because interrupt");
					return;
				}
				
				OutputStream fileOutputStream = null;
				try {
					fileOutputStream = new BufferedOutputStream(new FileOutputStream(tempFile, true));
					
					while (true) {
						InputStream networkInputStream = null;
						HttpURLConnection httpConn = null;
						
						if (Thread.interrupted()) {
							DebugLog.d(TAG, "[DownloadRunnable] exit because interrupt");
							return;
						}
						
						try {
							URL urll = new URL(url);
							httpConn = (HttpURLConnection) urll.openConnection();
							httpConn.setConnectTimeout(15000);
							httpConn.setReadTimeout(15000);
							httpConn.setRequestProperty("RANGE", "bytes=" + fileSize + "-");
							networkInputStream = httpConn.getInputStream();
							
							while (true) {
								if (Thread.interrupted()) {
									DebugLog.d(TAG, "[DownloadRunnable] exit because interrupt");
									return;
								}
								
								byte[] buffer = new byte[MP3_DOWNLOAD_BUFFER_SIZE];
								int num = networkInputStream.read(buffer);
								if (num < 0) {
									// 下载完了
									if (fileOutputStream != null) {
										try {
											try {
												fileOutputStream.flush();
											} catch (IOException e) {
											}
											
											fileOutputStream.close();
										} catch (IOException e) {
										}
										
										fileOutputStream = null;
									}
									
									File newFile = new File(getLocalPath(url));
									tempFile.renameTo(newFile);
									
									DebugLog.d(TAG, "[DownloadRunnable] finish file=" + newFile.getAbsolutePath());
									DebugLog.d(TAG, "[DownloadRunnable] exit because finish");
									
									return;
								} else {
									try {
										fileOutputStream.write(buffer, 0, num);
										fileOutputStream.flush();
									} catch (IOException e) {
										DebugLog.d(TAG, "[DownloadRunnable] the temp file write error");
										return;
									}
									
									try {
										mOutputStream.write(buffer, 0, num);
									} catch (IOException e1) {
										DebugLog.d(TAG, "[DownloadRunnable] the output write error");
										return;
									}
									
									fileSize += num;
								}
							}
						} catch (IOException e) {
							// 网络出现问题,3秒后重试,直接到finally后面运行
						} finally {
							if (networkInputStream != null) {
								try {
									networkInputStream.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							
							if (httpConn != null) {
								httpConn.disconnect();
							}
						}
						// 网络出现问题,3秒后重试
						DebugLog.d(TAG, "[DownloadRunnable] network error");
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							DebugLog.d(TAG, "[DownloadRunnable] exit because interrupt");
							return;
						}
					}
				} catch (FileNotFoundException e) {
					DebugLog.d(TAG, "[DownloadRunnable] the exist file open to write error");
					return;
				} finally {
					if (fileOutputStream != null) {
						try {
							fileOutputStream.close();
						} catch (IOException e) {
						}
					}
				}
			} finally {
				try {
					try {
						mOutputStream.flush();
					} catch (IOException e) {
					}
					
					mOutputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	private class DecodeRunnable implements Runnable {
		private PipedInputStream mInputStream;
		private BlockingQueue<PcmData> mOutputQ;
		
		private DecodeRunnable(
				PipedInputStream inputStream,
				BlockingQueue<PcmData> outputQ) {
			mInputStream = inputStream;
			mOutputQ = outputQ;
		}
		
		@Override
		public void run() {
			DebugLog.d(TAG, "[DecodeRunnable] enter");
			
			Bitstream bitStream = new Bitstream(mInputStream);
			Decoder decoder = new Decoder();
			
			try {
				while (true) {
					if (Thread.interrupted()) {
						DebugLog.d(TAG, "[DecodeRunnable] exit because interrupt");
						return;
					}
					
					Header header = bitStream.readFrame();
					
					try {
						if (Thread.interrupted()) {
							DebugLog.d(TAG, "[DecodeRunnable] exit because interrupt");
							return;
						}
						
						if (header == null) {
							// 解码完了
							PcmData pcmData = new PcmData();
							pcmData.mLength = DATA_LENGTH_FINISHED;
							
							try {
								mOutputQ.put(pcmData);
								DebugLog.d(TAG, "[DecodeRunnable] exit because finish");
								return;
							} catch (InterruptedException e) {
								DebugLog.d(TAG, "[DecodeRunnable] exit because interrupt");
								return;
							}
						} else {
							SampleBuffer sampleBuffer
								= (SampleBuffer) decoder.decodeFrame(header, bitStream);
							
							if (Thread.interrupted()) {
								DebugLog.d(TAG, "[DecodeRunnable] exit because interrupt");
								return;
							}
							
							PcmData pcmData = new PcmData();
							pcmData.mLength = sampleBuffer.getBufferLength();
							pcmData.mData = Arrays.copyOf(
									sampleBuffer.getBuffer(), pcmData.mLength);
							pcmData.mSampleRateInHz = sampleBuffer.getSampleFrequency();
							pcmData.mIsMono = sampleBuffer.getChannelCount() == 1;
							
							try {
								mOutputQ.put(pcmData);
							} catch (InterruptedException e) {
								DebugLog.d(TAG, "[DecodeRunnable] exit because interrupt");
								return;
							}
						}
					} finally {
						bitStream.closeFrame();
					}
				}
			} catch (BitstreamException e) {
				PcmData pcmData = new PcmData();
				pcmData.mLength = DATA_LENGTH_ERROR;
				
				try {
					mOutputQ.put(pcmData);
					DebugLog.d(TAG, "[DecodeRunnable] exit because error");
				} catch (InterruptedException e1) {
					DebugLog.d(TAG, "[DecodeRunnable] exit because interrupt");
				}
			} catch (DecoderException e) {
				PcmData pcmData = new PcmData();
				pcmData.mLength = DATA_LENGTH_ERROR;
				
				try {
					mOutputQ.put(pcmData);
					DebugLog.d(TAG, "[DecodeRunnable] exit because error");
				} catch (InterruptedException e1) {
					DebugLog.d(TAG, "[DecodeRunnable] exit because interrupt");
				}
			} finally {
				try {
					bitStream.close();
				} catch (BitstreamException e) {
				}
				
				try {
					mInputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	private class PlayRunnable implements Runnable {
		private BlockingQueue<PcmData> mInputQ;
		private Playable mCurrPlayable;
		
		private PlayRunnable(Playable playable, BlockingQueue<PcmData> inputQ) {
			mInputQ = inputQ;
			mCurrPlayable = playable;
		}
		
		@Override
		public void run() {
			DebugLog.d(TAG, "[PlayRunnable] enter");
			
			AudioTrack audioTrack = null;
			
			try {
				double totalLenght = 0;
				int lastMs = 0;
				
				double bufferedPcmDataLength = 0;
				List<PcmData> bufferedPcmData = new ArrayList<PcmData>();
				
				while(true) {
					if (Thread.interrupted()) {
						DebugLog.d(TAG, "[PlayRunnable] exit because interrupt");
						return;
					}
					
					mHandler.sendMessageDelayed(mHandler.obtainMessage(WHAT_LOADING, mCurrPlayable), 500);
					
					PcmData pcmData = mInputQ.take();
					
					if (pcmData.mLength == DATA_LENGTH_ERROR) {
						// 播放完之前缓冲的
						if (audioTrack != null) {
							for (PcmData data : bufferedPcmData) {
								if (Thread.interrupted()) {
									DebugLog.d(TAG, "[PlayRunnable] exit because interrupt");
									return;
								}
								
								audioTrack.write(data.mData, 0, data.mLength);
								
								mHandler.removeMessages(WHAT_LOADING);
								mHandler.removeMessages(WHAT_START);
								mHandler.sendMessage(mHandler.obtainMessage(WHAT_START, mCurrPlayable));
								
								totalLenght += data.mLength;
								int ms = (int) (totalLenght / (audioTrack.getSampleRate() * audioTrack.getChannelCount()) * 1000);
								
								if ((ms - lastMs) > PROGRESS_PERIOD_MS) {
									mHandler.sendMessage(mHandler.obtainMessage(WHAT_PROGRESS_PCM, ms, 0, mCurrPlayable));
									lastMs = ms;
								}
							}
							
							audioTrack.write(new short[0], 0, 0);
						}
						
						DebugLog.d(TAG, "[PlayRunnable] exit because error");
						mHandler.sendMessage(mHandler.obtainMessage(WHAT_ERROR, mCurrPlayable));
						return;
					}
					
					if (pcmData.mLength == DATA_LENGTH_FINISHED) {
						// 播放完之前缓冲的
						if (audioTrack != null) {
							for (PcmData data : bufferedPcmData) {
								if (Thread.interrupted()) {
									DebugLog.d(TAG, "[PlayRunnable] exit because interrupt");
									return;
								}
								
								audioTrack.write(data.mData, 0, data.mLength);
								
								mHandler.removeMessages(WHAT_LOADING);
								mHandler.removeMessages(WHAT_START);
								mHandler.sendMessage(mHandler.obtainMessage(WHAT_START, mCurrPlayable));
								
								totalLenght += data.mLength;
								int ms = (int) (totalLenght / (audioTrack.getSampleRate() * audioTrack.getChannelCount()) * 1000);
								
								if ((ms - lastMs) > PROGRESS_PERIOD_MS) {
									mHandler.sendMessage(mHandler.obtainMessage(WHAT_PROGRESS_PCM, ms, 0, mCurrPlayable));
									lastMs = ms;
								}
							}
							
							audioTrack.write(new short[0], 0, 0);
						}
						
						DebugLog.d(TAG, "[PlayRunnable] exit because finished");
						// 推迟一定时间进入完成状态
						int dur = (int) mCurrPlayable.getSoundDuration();
						mHandler.sendMessage(mHandler.obtainMessage(WHAT_PROGRESS_PCM, dur, 0, mCurrPlayable));
						mHandler.sendMessageDelayed(mHandler.obtainMessage(WHAT_FINISHED, mCurrPlayable), DELAY_FINISHED_MS);
						return;
					}
					
					if (needNewAudioTrack(audioTrack, pcmData)) {
						// 播放完之前的缓冲并释放之前的资源
						if (audioTrack != null) {
							for (PcmData data : bufferedPcmData) {
								if (Thread.interrupted()) {
									DebugLog.d(TAG, "[PlayRunnable] exit because interrupt");
									return;
								}
								
								audioTrack.write(data.mData, 0, data.mLength);
								
								mHandler.removeMessages(WHAT_LOADING);
								mHandler.removeMessages(WHAT_START);
								mHandler.sendMessage(mHandler.obtainMessage(WHAT_START, mCurrPlayable));
								
								totalLenght += data.mLength;
								int ms = (int) (totalLenght / (audioTrack.getSampleRate() * audioTrack.getChannelCount()) * 1000);
								
								if ((ms - lastMs) > PROGRESS_PERIOD_MS) {
									mHandler.sendMessage(mHandler.obtainMessage(WHAT_PROGRESS_PCM, ms, 0, mCurrPlayable));
									lastMs = ms;
								}
							}
							
							audioTrack.write(new short[0], 0, 0);
							
							audioTrack.pause();
							audioTrack.flush();
							audioTrack.release();
						}
						
						DebugLog.d(TAG, "[PlayRunnable] new AudioTrack");
						
						int bufferSizeInBytes = AudioTrack.getMinBufferSize(
								pcmData.mSampleRateInHz,
								pcmData.mIsMono ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
								AudioFormat.ENCODING_PCM_16BIT);
						
						if ((bufferSizeInBytes == AudioTrack.ERROR_BAD_VALUE) ||
								(bufferSizeInBytes == AudioTrack.ERROR)) {
							DebugLog.d(TAG, "[PlayRunnable] exit because meta error");
							mHandler.sendMessage(mHandler.obtainMessage(WHAT_ERROR, mCurrPlayable));
							return;
						}
						
						audioTrack = new AudioTrack(
								AudioManager.STREAM_MUSIC,
								pcmData.mSampleRateInHz,
								pcmData.mIsMono ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
								AudioFormat.ENCODING_PCM_16BIT,
								bufferSizeInBytes,
								AudioTrack.MODE_STREAM);
						
						audioTrack.play();
						
						bufferedPcmDataLength = 0;
						bufferedPcmData.clear();
					}
					
					bufferedPcmDataLength += pcmData.mLength;
					bufferedPcmData.add(pcmData);
					
					int bufferedMs = (int) (bufferedPcmDataLength / (audioTrack.getSampleRate() * audioTrack.getChannelCount()) * 1000);
					if (bufferedMs >= MIN_BUFFER_TIME_MS_TO_PLAY) {
						// 开始播放之前缓冲的
						for (PcmData data : bufferedPcmData) {
							if (Thread.interrupted()) {
								DebugLog.d(TAG, "[PlayRunnable] exit because interrupt");
								return;
							}
							
							audioTrack.write(data.mData, 0, data.mLength);
							
							mHandler.removeMessages(WHAT_LOADING);
							mHandler.removeMessages(WHAT_START);
							mHandler.sendMessage(mHandler.obtainMessage(WHAT_START, mCurrPlayable));
							
							totalLenght += data.mLength;
							int ms = (int) (totalLenght / (audioTrack.getSampleRate() * audioTrack.getChannelCount()) * 1000);
							
							if ((ms - lastMs) > PROGRESS_PERIOD_MS) {
								mHandler.sendMessage(mHandler.obtainMessage(WHAT_PROGRESS_PCM, ms, 0, mCurrPlayable));
								lastMs = ms;
							}
						}
						
						bufferedPcmDataLength = 0;
						bufferedPcmData.clear();
					} else {
						// 继续缓冲
						continue;
					}
				}
			} catch (InterruptedException e) {
				mHandler.removeMessages(WHAT_LOADING);
				DebugLog.d(TAG, "[PlayRunnable] exit because interrupt");
				return;
			} finally {
				if (audioTrack != null) {
					audioTrack.pause();
					audioTrack.flush();
					audioTrack.release();
				}
			}
		}
		
		private boolean needNewAudioTrack(AudioTrack audioTrack, PcmData pcmData) {
			if (audioTrack == null) {
				return true;
			}
			
			if (pcmData.mSampleRateInHz != audioTrack.getSampleRate()) {
				return true;
			}
			
			if ((pcmData.mIsMono) &&
					(audioTrack.getChannelConfiguration() == AudioFormat.CHANNEL_OUT_STEREO)) {
				return true;
			}
			
			if ((!pcmData.mIsMono) &&
					(audioTrack.getChannelConfiguration() == AudioFormat.CHANNEL_OUT_MONO)) {
				return true;
			}
			
			return false;
		}
	}
	
	private class InCallModeChangeListener implements ModeChangeListener {
		private int mMode;
		
		public void setInitMode(int mode) {
			mMode = mode;
		}
		
		@Override
		public void onModeChanged(int mode) {
			if ((mMode != mode)
					&& (mState != State.FINISHED)
					&& (mPlayable != null)
					&& (mPlayable.getSoundDuration() <= MIN_DURATION_TO_REPLAY)) {
				// 重新播放
				DebugLog.d(TAG, "[onModeChanged()] replay because sensor: " + mPlayable);
				playInternal(mPlayable, false, false);
			}
			
			mMode = mode;
		}
	}
}
