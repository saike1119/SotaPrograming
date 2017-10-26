package jp.vstone.sotasample;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

import jp.vstone.RobotLib.CPlayWave;
import jp.vstone.RobotLib.CRobotMem;
import jp.vstone.RobotLib.CRobotPose;
import jp.vstone.RobotLib.CRobotUtil;
import jp.vstone.RobotLib.CSotaMotion;
import jp.vstone.camera.CRoboCamera;
import jp.vstone.sotatalk.SpeechRecog;
import jp.vstone.sotatalk.TextToSpeechSota;

public class CommunicationSota {
	static final String TAG = "SpeechRecSample";
	// VSMDと通信ソケット・メモリアクセス用クラス
	private static CRobotMem mem = new CRobotMem();
	private static CSotaMotion motion = new CSotaMotion(mem);
	// Sota用モーション制御クラス
	private static SpeechRecog recog = new SpeechRecog(motion);
	// 話す内容のファイルを定義
	private static final String wav_file = "./temp.wav";
	private static boolean isGetWavFile = false;
	static final int SMILE_POINT = 45;

	// メイン
	public static void main(String[] args) {
		CRobotUtil.Log(TAG, "Start " + TAG);

		CRobotPose pose;
		// VSMDと通信ソケット・メモリアクセス用クラス
		CRobotMem mem = new CRobotMem();
		// Sota用モーション制御クラス
		CSotaMotion motion = new CSotaMotion(mem);

		CRoboCamera cam = new CRoboCamera("/dev/video0", motion);

		if (mem.Connect()) {
			// Sota仕様にVSMDを初期化
			motion.InitRobot_Sota();

			CRobotUtil.Log(TAG, "Rev. " + mem.FirmwareRev.get());

			// サーボモータを現在位置でトルクOnにする
			CRobotUtil.Log(TAG, "Servo On");
			motion.ServoOn();

			// すべての軸を動作
			pose = new CRobotPose();
			pose.SetPose(new Byte[] { 1, 2, 3, 4, 5, 6, 7, 8 } // id
					, new Short[] { 0, -900, 0, 900, 0, 0, 0, 0 } // target pos
			);
			// LEDを点灯（左目：赤、右目：赤、口：Max、電源ボタン：赤）
			pose.setLED_Sota(Color.BLUE, Color.BLUE, 255, Color.GREEN);

			motion.play(pose, 500);
			CRobotUtil.wait(500);

			while (true) {
				// 指定の挨拶がされるまでステイし続ける
				rndHelloSota();

				// LEDだけ先に変更
				pose.setLED_Sota(Color.ORANGE, Color.ORANGE, 255, Color.GREEN);
				// playに任意のKeyを指定すると、
				motion.play(pose, 100, "FACE_LED");

				pose = new CRobotPose();
				// 頭を動かさずに撮影する -> 頭の角度を指定しない
				pose.SetPose(new Byte[] { 1, 2, 3, 4, 5 }, new Short[] { 0, -900, 0, 900, 0 });
				motion.play(pose, 1000);

				String hello = recog.getResponse(15000, 10);
				if (hello.equals("こんにちは") || hello.equals("こんばんは") || hello.equals("おはよう")) {
					pose = new CRobotPose();
					pose.SetPose(new Byte[] { 1, 2, 3, 4, 5, 6, 7, 8 } // id
							, new Short[] { 0, -900, 0, 900, 0, 0, 0, 0 } // target
																			// pos
					);
					pose.setLED_Sota(Color.BLUE, Color.BLUE, 255, Color.BLUE);
					motion.play(pose, 1000);
					helloQuestionSota(hello);
					String name = recog.getName(15000, 3);
					if (name != null) {
						helloNameSota(name);
						// おしゃべりかおみくじを分岐選択
						String select = recog.getResponse(15000, 100);
						if (select.equals("おしゃべり")) {
							// 写真を取る前のポーズ＋音声
							pose = new CRobotPose(); // @<BlockInfo>jp.vstone.block.pose,208,80,208,80,False,2,コメント@</BlockInfo>
							pose.SetPose(new Byte[] { 1, 2, 3, 4, 5 }, new Short[] { -1, 71, -895, 1, 769 });
							motion.play(pose, 1000);
							CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("おっけー！おしゃべりしよう！僕が聞きたいこと聞くね〜"), true);
							// 話題の番号をランダムで生成する
							Random rnd = new Random();
							int wRan = rnd.nextInt(3) + 1;
							// TODO:開発途中の話題
							if (wRan == 1) {
								wadai1(name);
							}
							// TODO:開発途中の話題
							if (wRan == 2) {
								wadai2();
							}
							// TODO:開発途中の話題
							if (wRan == 3) {
								wadai3();
							}
						}
						// おみくじ
						if (select.equals("おみくじ")) {
							omikuziSota();
						}
						// おみくじ
						if (select.equals("歌") || select.equals("うた") || select.equals("お歌") || select.equals("おうた")) {
							pose = new CRobotPose();
							// 頭を動かさずに撮影する -> 頭の角度を指定しない
							// //@<BlockInfo>jp.vstone.block.pose,272,80,272,80,False,1,コメント@</BlockInfo>
							pose.SetPose(new Byte[] { 1, 2, 3, 4, 5 }, new Short[] { 1, 69, -21, 3, -35 });
							motion.play(pose, 1000);
							songSota();
						}
						// 会話終了
						finishCommunication();
					}
				}
			}
		}
	}

	// functions
	public static void helloQuestionSota(String hello) {
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(hello + "、そーたです！"), true);
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("まず、あなたのお名前はなんていうの？"), true);
	}

	public static void helloNameSota(String name) {
		CRobotUtil.Log(TAG, name);
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(name + "さんっていうんだね。よろしくね！"), true);
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("僕はおしゃべりとおみくじと歌が歌えるけど、何したらいいかな？"), true);
	}

	// TODO:開発途中のファンクション
	public static void wadai1(String name) {
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ちなみにお肉とお魚、どっちが好き？"), true);
		String food = recog.getResponse(15000, 3);
		// 焼肉か寿司か分岐点
		if (food != null) {
			CRobotUtil.Log(TAG, food);
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(name + "さんは" + food + "が好きなんだね"), true);
			// 寿司ルート
			if (food.equals("お魚") || food.equals("おさかな") || food.equals("オサカナ")) {
				CRobotUtil.Log(TAG, food);
				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("僕もだよ、お寿司美味しいよね〜"), true);
				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("マグロかシメサバだったらどっちが好き？"), true);
				String neta = recog.getResponse(15000, 3);
				// ネタ分岐
				if (food.equals("マグロ")) {
					CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("王道だね！"), true);
					CRobotUtil.Log(TAG, neta);
				}
				if (food.equals("しめさば")) {
					CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("渋いね〜"), true);
					CRobotUtil.Log(TAG, neta);
				} else {
					CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("それも美味しそうだね"), true);
					CRobotUtil.Log(TAG, neta);
				}

				// 焼肉ルート
			} else if (food.equals("お肉") || food.equals("おにく") || food.equals("オニク")) {
				CRobotUtil.Log(TAG, food);
				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("焼肉いいよね"), true);
				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("茅ヶ崎駅に、ざんまいっていう美味しい焼肉屋さんがあるよ"), true);
				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ぜひ、行ってみて！"), true);

				// そのほかの食べ物を答えたとき
			} else {
				CRobotUtil.Log(TAG, food);
				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(food + "って食べ物があるの知らなかったよ、美味しそうだね。"), true);
				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ありがとう！"), true);
			}
		}
	}

	// TODO:開発途中のファンクション
	public static void wadai2() {
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("最近はちゃんと寝てる？？"), true);

		String geinou = recog.getResponse(15000, 3);
		if (geinou != null) {
			CRobotUtil.Log(TAG, geinou);
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("夜はちゃんと寝たほうがいいいよ〜。おやすみ！"), true);
		}
	}

	// TODO:開発途中のファンクション
	public static void wadai3() {
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("アンドロイドとアイフォンどっち使ってる？"), true);

		String sumaho = recog.getResponse(15000, 3);
		if (sumaho != null) {
			CRobotUtil.Log(TAG, sumaho);
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(sumaho + "を使ってるんだね。いいね！"), true);
		}
	}

	/*
	 * 確率でおみくじをする
	 */
	public static void omikuziSota() {
		// 確率をランダムで生成する
		Random rnd = new Random();
		int oRan = rnd.nextInt(100) + 1;
		// 出た数を表示
		// System.out.println(ran);

		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("おっけー！おみくじだね！"), true);
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("今からおみくじを僕の中で引くね！いいものが当たるといいね"), true);
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ガララララララララララララララララララ、ダン！"), true);

		if (oRan >= 80) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("吉"), true);
		} else if (oRan < 80 && oRan >= 95) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("凶"), true);
		} else if (oRan < 95 && oRan >= 100) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("大吉"), true);
		}

		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("おめでとう〜〜！また来年も来てね"), true);
	}

	public static void finishCommunication() {
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("じゃあ、またね〜〜〜！"), true);
	}

	/*
	 * POSTにより音声合成サーバでWAVファイルを作成するための処理
	 */
	public static boolean getWavFileByCallGET(String strPostUrl, String param, String target) {

		HttpURLConnection con = null;

		try {

			URL url = new URL(strPostUrl + "?" + param);
			// System.out.println(url.toString());

			InputStream in = null;

			con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("GET");

			con.connect(); // URLに文字列パラメータを追加してGETでリクエストを送信

			// HTTPレスポンスコードを取得
			final int status = con.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK) { // 通信に成功した
				// バイナリファイルとしての保存
				in = con.getInputStream();
				if (in != null) {
					// バイナリ形式でファイルを保存
					FileOutputStream fos = new FileOutputStream(new File(target));
					int line = -1;

					while ((line = in.read()) != -1) {
						fos.write(line);
					}
					in.close();
					fos.close();
				}

			} else {
				System.out.println(con.getResponseMessage());
				return false;
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (con != null) {
				// コネクションを切断
				con.disconnect();
			}
		}

		return true;

	}

	/*
	 * Webサーバにアクセスしてテキスト(String)をGETで取得する処理 ※以下の参考サイトのコードを一部改変
	 * http://web.plus-idea.net/2016/08/httpurlconnection-post-get-proxy-sample/
	 */
	public static String getStringByCallGET(String strGetUrl) {

		HttpURLConnection con = null;
		StringBuffer result = new StringBuffer();

		try {

			URL url = new URL(strGetUrl);

			con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod("GET");
			con.connect(); // URLにGETでリクエストを送信

			// HTTPレスポンスコード
			final int status = con.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK) {
				// 通信に成功した
				// テキストを取得する
				final InputStream in = con.getInputStream();
				String encoding = con.getContentEncoding();
				if (null == encoding) {
					encoding = "UTF-8";
				}
				final InputStreamReader inReader = new InputStreamReader(in, encoding);
				final BufferedReader bufReader = new BufferedReader(inReader);
				String line = null;
				// 1行ずつテキストを読み込む
				while ((line = bufReader.readLine()) != null) {
					result.append(line);
				}
				bufReader.close();
				inReader.close();
				in.close();
			} else {
				// System.out.println(status);
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (con != null) {
				// コネクションを切断
				con.disconnect();
			}
		}
		// System.out.println("result=" + result.toString());

		return result.toString();
	}

	public static void connectionHTTP() {
		try {
			// しゃべる内容をサーバ側のテキストファイルから取得
			// String getText_url = "http://172.20.5.58/~b4p31047/temp.txt";
			// .txt以外にPHPやRubyなどでもOK（一行でUTF-8のテキストが返ってくれば，なんでも可）
			String getText_url = "http://133.130.107.245/temp.txt"; // .txt以外にPHPやRubyなどでもOK（一行でUTF-8のテキストが返ってくれば，なんでも可）
			String speech_text = "テキスト取得，エラーです";

			speech_text = getStringByCallGET(getText_url); // ローカルでのテキスト取得も可能ですが，Sota側の計算能力は貧弱です

			// String TTS_url =
			// "http://172.20.5.58/~hidenao/tts_voice_text.php";
			// VoiceTextを実行するPHPスクリプトのURL
			String TTS_url = "http://133.130.107.245/tts_voice_text.php"; // VoiceTextを実行するPHPスクリプトのURL
			// String HTTP_contentType = "application/x-www-form-urlencoded;";
			String encodeStr = URLEncoder.encode(speech_text, "utf-8");

			// emotion, emotion_level, speakerなどのパラメータを事前に用意して変更することもできる

			String param = "method=post&emotion=happiness&emotion_level=2&speaker=hikari&text=" + encodeStr; // VoiceTextにPOSTする各種パラメータを設定
			// GETでVoice Text Web
			// APIを利用するPHPスクリプトにアクセス（通常はリクエスト文字列長8KBまでのためPOSTへの変更も可能）
			isGetWavFile = getWavFileByCallGET(TTS_url, param, wav_file);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// 音声ファイルが取得できたら音声ファイルを再生（再生し，再生終了まで待つ）
		if (isGetWavFile) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(wav_file), true);
			// CPlayWave.PlayWave_wait(wav_file);
		}
	}

	public static void getTextSpeech() {
		String getText_url = "http://133.130.107.245/temp.txt";
		String speech_text = "テキスト取得，エラーです";

		speech_text = getStringByCallGET(getText_url);
		if (speech_text != null) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(speech_text), true);
		}
	}

	public static void songSota() {
		CPlayWave.PlayWave_wait("./sound/song.wav");
	}

	public static void rndHelloSota() {
	}
}
