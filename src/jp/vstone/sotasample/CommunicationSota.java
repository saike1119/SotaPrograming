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

		if (mem.Connect()) {
			// Sota仕様にVSMDを初期化
			motion.InitRobot_Sota();

			CRobotUtil.Log(TAG, "Rev. " + mem.FirmwareRev.get());

			// サーボモータを現在位置でトルクOnにする
			CRobotUtil.Log(TAG, "Servo On");
			motion.ServoOn();

			// 全ての軸を初期化
			pose = new CRobotPose();
			pose.SetPose(new Byte[] { 1, 2, 3, 4, 5, 6, 7, 8 } // id
					, new Short[] { 0, -900, 0, 900, 0, 0, 0, 0 } // target pos
			);
			// LEDを点灯（左目：赤、右目：赤、口：Max、電源ボタン：赤）
			pose.setLED_Sota(Color.ORANGE, Color.ORANGE, 255, Color.ORANGE);

			motion.play(pose, 100);
			CRobotUtil.wait(100);

			while (true) {
				// 指定の挨拶がされるまでステイし続ける
				String hello = recog.getResponse(15000, 100);
				if (hello.contains("こんにちは") || hello.contains("こんばんは") || hello.contains("おはよ")
						|| hello.contains("ハロー")) {
					// 可愛いポーズ
					pose.SetPose(new Byte[] { 1, 2, 3, 4, 5 }, new Short[] { 0, 180, -850, -180, 850 });
					motion.play(pose, 1000);
					CRobotUtil.wait(100);
					helloQuestionSota(hello);

					String name = recog.getName(15000, 100);
					if (name != null) {
						// 左手を挙げるポーズ
						pose.SetPose(new Byte[] { 1, 2, 3, 4, 5 }, new Short[] { 0, 800, 0, 900, 0 });
						motion.play(pose, 1000);
						CRobotUtil.wait(100);
						helloNameSota(name);
						// おしゃべりかおみくじを分岐選択
						String select = recog.getResponse(15000, 100);
						if (select.contains("しゃべり")) {
							// 右手を挙げるポーズ
							pose.SetPose(new Byte[] { 1, 2, 3, 4, 5 }, new Short[] { 0, -900, 0, -800, 0 });
							motion.play(pose, 1000);
							CRobotUtil.wait(100);
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
								wadai2();
							}
						}
						// おみくじ
						if (select.contains("おみくじ") || select.contains("くじ")) {
							pose.SetPose(new Byte[] { 1, 2, 3, 4, 5 }, new Short[] { 0, 180, 0, -180, 0 });
							motion.play(pose, 1000);
							CRobotUtil.wait(100);
							omikuziSota();
						}
						// 歌う
						if (select.contains("歌") || select.contains("うた") || select.contains("唄")) {
							// 両手を広げるポーズ
							pose.SetPose(new Byte[] { 1, 2, 3, 4, 5 }, new Short[] { 0, 180, 0, -180, 0 });
							motion.play(pose, 1000);
							CRobotUtil.wait(100);
							songSota();
						}
						if (select.contains("ヤフー") || select.contains("ニュース") || select.contains("読み上げ")
								|| select.contains("ｙａｈｏｏ")) {
							pose.SetPose(new Byte[] { 1, 2, 3, 4, 5 }, new Short[] { 0, 180, 0, -180, 0 });
							motion.play(pose, 1000);
							CRobotUtil.wait(100);
							speechYahooNews();
						}
						finishCommunication();
						// 全ての軸を初期化
						pose = new CRobotPose();
						pose.SetPose(new Byte[] { 1, 2, 3, 4, 5 }, new Short[] { 0, -900, 0, 900, 0, });
						motion.play(pose, 1000);
						CRobotUtil.wait(100);
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
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("僕はおしゃべりと、おみくじと、ヤフーニュース読み上げと、歌が歌えるけど、何をしたらいいかな〜？"), true);
	}

	// TODO:開発途中のファンクション
	public static void wadai1(String name) {
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ちなみに焼肉と寿司、どっちが好き？"), true);
		String food = recog.getResponse(15000, 100);
		CRobotUtil.Log(TAG, food);
		// 寿司ルート
		if (food.contains("寿司") || food.contains("すし")) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(name + "さんはお寿司が好きなんだね"), true);
			CRobotUtil.Log(TAG, food);
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("僕もだよ、お寿司美味しいよね〜"), true);
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("マグロかサーモンだったらどっちが好き？"), true);
			String neta = recog.getResponse(15000, 100);
			// ネタ分岐
			if (food.contains("マグロ") || food.contains("まぐろ")) {
				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("王道だね！僕もまぐろ大好きだよ〜！"), true);
				CRobotUtil.Log(TAG, neta);
			}
			if (food.contains("サーモン") || food.contains("さーもん")) {
				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("サーモン！美味しいよね！脂乗ってて最高だよね！"), true);
				CRobotUtil.Log(TAG, neta);
			}
		}

		// 焼肉ルート
		if (food.contains("肉") || food.contains("にく")) {
			CRobotUtil.Log(TAG, food);
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(name + "さんは焼肉が好きなんだね"), true);
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("焼肉いいよね！"), true);
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("茅ヶ崎駅にと湘南台駅に、ざんまいっていう美味しい焼肉屋さんがあるよ！安くていっぱい食べれるんだよ！"), true);
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("サラダもたくさんでいつもお腹いっぱいになっちゃうんだよ〜！"), true);
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ぜひ、行ってみてね！"), true);
		}

	}

	// TODO:開発途中のファンクション
	public static void wadai2() {
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("文化祭はどう〜？楽しんでる？？"), true);

		String festival = recog.getResponse(15000, 100);
		if (festival.contains("うん") || festival.contains("はい") || festival.contains("楽しん") || festival.contains("でる")) {
			CRobotUtil.Log(TAG, festival);
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("わーい！よかった！ぬるつーぶーすではもう遊んでみた？"), true);
			String yesorno = recog.getResponse(15000, 100);
			if (yesorno.contains("うん") || yesorno.contains("はい") || yesorno.contains("みた")) {
				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ありがとうね！楽しい展示作品いっぱいあったでしょ〜？また来年もきてね！！"), true);
			}
			if (yesorno.contains("いいえ") || yesorno.contains("まだ") || yesorno.contains("これから")
					|| yesorno.contains("ない")) {
				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("楽しい展示作品いっぱいあるからまわってみて！！！"), true);
				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("僕との約束だよ〜〜〜！"), true);
			}
		}
		if (festival.contains("いいえ") || festival.contains("まだ") || festival.contains("これから") || festival.contains("遊んで")
				|| festival.contains("ない")) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("楽しい展示作品いっぱいあるからまわってみて！！！"), true);
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("僕との約束だよ〜〜〜！"), true);
		}
	}

	// TODO:開発途中のファンクション
	public static void wadai3() {
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ゲームは好きかな？"), true);

		String example = recog.getResponse(15000, 100);
		if (example.contains("うん") || example.contains("はい")) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ゲーム楽しいよね。良いね。"), true);
		}
		if (example.contains("いいえ") || example.contains("違う")) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ゲーム楽しいよ。遊んでいってね。"), true);
		}
	}

	/*
	 * 確率でおみくじをする
	 */
	public static void omikuziSota() {

		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("おっけー！おみくじだね！"), true);
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("今からおみくじを僕の中で引くね！いいものが当たるといいね"), true);
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ガララララララララララララララララララ、ダン！"), true);

		// 確率をランダムで生成する
		Random rnd = new Random();
		int oRan = rnd.nextInt(100) + 1;
		// 両手を広げるポーズ

		if (oRan <= 80) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("大吉だよ！"), true);
		}
		if (oRan > 80 && oRan <= 95) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("吉だよ！"), true);
		}
		if (oRan > 95 && oRan <= 100) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("凶だよ！"), true);
		}

		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("おめでとう〜〜！また来年も来てね"), true);
	}

	public static void finishCommunication() {
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("じゃあ、またね〜！"), true);
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

	public static void speechYahooNews() {
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("今、頑張って最新のヤフーニュースを覚えているよ！ちょっと待ってね。"), true);
		String getText_url = "http://133.130.107.245/sota.txt";
		String speech_text = "テキスト取得，エラーです";

		speech_text = getStringByCallGET(getText_url);
		if (speech_text != null) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(speech_text), true);
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("終わりだよ！色々なことがあるね〜！ニュースって面白いね！"), true);
		}
	}

	public static void songSota() {
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("それじゃあ、歌うね！おっほん！"), true);
		CPlayWave.PlayWave_wait("./sound/song_short.wav");
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("どうだったかな〜？うまいでしょ〜？"), true);
	}

	public static void rndHelloSota() {
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("こんにちは〜〜！こちらぬるつーブースです。ゆっくりしていってね！"), true);
	}
}
