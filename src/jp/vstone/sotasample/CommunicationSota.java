package jp.vstone.sotasample;

import java.util.Random;

import jp.vstone.RobotLib.CPlayWave;
import jp.vstone.RobotLib.CRobotMem;
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
	// ランダムな数をインスタンス化
	private static Random rnd = new Random();

	// メイン
	public static void main(String[] args) {
		// 話題の番号をランダムで生成する
		int ran = rnd.nextInt(3) + 1;
		if (mem.Connect()) {
			// Sota仕様にVSMDを初期化
			motion.InitRobot_Sota();
			while (true) {
				// 指定の挨拶がされるまでステイし続ける
				String hello = recog.getResponse(15000, 1000);
				if (hello.equals("こんにちは") || hello.equals("こんばんは") || hello.equals("おはよう")) {
					helloQuestionSota(hello);
					String name = recog.getName(15000, 3);
					if (name != null) {
						helloNameSota(name);
						// おしゃべりかおみくじを分岐選択
						String select = recog.getResponse(15000, 100);
						if (select.equals("おしゃべり")) {
							CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("おっけー！おしゃべりしよう！僕が聞きたいこと聞くね〜"), true);
							// TODO:開発途中の話題
							if (ran == 1) {
								wadai1(name);
							}
							// TODO:開発途中の話題
							if (ran == 2) {
								wadai2();
							}
							// TODO:開発途中の話題
							if (ran == 3) {
								wadai3();
							}
						}
						// おみくじ
						if (select.equals("おみくじ")) {
							omikuziSota();
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
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("僕はおしゃべりとおみくじができるけど、どっちをしたいかな〜？"), true);
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

	// おみくじ機能
	public static void omikuziSota() {
		// 確率をランダムで生成する
		int ran = rnd.nextInt(100) + 1;
		// 出た数を表示
		System.out.println(ran);

		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("おっけー！おみくじだね！"), true);
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("今からおみくじを僕の中で引くね！いいものが当たるといいね"), true);
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ガララララララララララララララララララ、ダン！"), true);

		if (ran >= 80) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("飴ちゃんが当たったよ！"), true);
		} else if (ran < 80 && ran >= 95) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("うまい棒が当たったよ！"), true);
		} else if (ran < 95 && ran >= 100) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("大当たり！チョコボールが当たったよ！"), true);
		}

		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("おめでとう〜〜！また来年も来てね"), true);
	}

	public static void finishCommunication() {
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("じゃあ、またね〜〜〜！"), true);
	}
}
