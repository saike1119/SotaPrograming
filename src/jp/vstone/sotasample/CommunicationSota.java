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

	// private static final String ロ1 = null;
	// private static String ;
	public static void main(String[] args) {
		// VSMDと通信ソケット・メモリアクセス用クラス
		CRobotMem mem = new CRobotMem();
		// Sota用モーション制御クラス
		CSotaMotion motion = new CSotaMotion(mem);
		SpeechRecog recog = new SpeechRecog(motion);
		if (mem.Connect()) {
			// Sota仕様にVSMDを初期化
			motion.InitRobot_Sota();

			while (true) {
				// 指定の挨拶がされるまでステイし続ける
				String hello = recog.getResponse(15000, 100);
				if (hello.equals("こんにちは") || hello.equals("こんばんは") || hello.equals("おはよう")) {

					CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(hello + "、そーたです！"), true);
					CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("まず、あなたのお名前はなんていうの？"), true);

					// 話題の番号をランダムで生成する
					Random rnd = new Random();
					int ran = rnd.nextInt(3);

					String name = recog.getName(15000, 3);
					if (name != null) {
						CRobotUtil.Log(TAG, name);
						CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(name + "さんっていうんだね。よろしくね！"), true);
						CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("僕はおしゃべりとおみくじができるけど、どっちをしたいかな〜？"), true);

						// おしゃべりかおみくじか分岐選択
						String select = recog.getResponse(15000, 100);
						if (select.equals("おしゃべり")) {
							CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("おっけー！おしゃべりしよう！僕が聞きたいこと聞くね〜"), true);
							// TODO:開発途中の話題
							if (ran == 0) {
								CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ちなみに焼肉とお寿司、どっちが好き？"), true);
								String food = recog.getResponse(15000, 3);
								// 焼肉か寿司か分岐点
								if (food != null) {
									CRobotUtil.Log(TAG, food);
									CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(name + "さんは" + food + "が好きなんだね"),
											true);
									// 寿司ルート
									if (food.equals("お寿司") || food.equals("おすし")) {
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
									} else if (food.equals("焼肉")) {
										CRobotUtil.Log(TAG, food);
										CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("焼肉いいよね"), true);
										CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("茅ヶ崎駅に、ざんまいっていう美味しい焼肉屋さんがあるよ"),
												true);
										CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ぜひ、行ってみて！"), true);

										// そのほかの食べ物を答えたとき
									} else {
										CRobotUtil.Log(TAG, food);
										CPlayWave.PlayWave(
												TextToSpeechSota.getTTSFile(food + "って食べ物があるの知らなかったよ、美味しそうだね。"), true);
										CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ありがとう！"), true);
									}
								}
							}
							// TODO:開発途中の話題
							if (ran == 1) {
								CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("最近はちゃんと寝てる？？"), true);

								String geinou = recog.getResponse(15000, 3);
								if (geinou != null) {
									CRobotUtil.Log(TAG, geinou);
									CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("夜はちゃんと寝たほうがいいいよ〜。おやすみ！"), true);
								}
							}
							// TODO:開発途中の話題
							if (ran == 2) {
								CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("アンドロイドとアイフォンどっち使ってる？"), true);

								String sumaho = recog.getResponse(15000, 3);
								if (sumaho != null) {
									CRobotUtil.Log(TAG, sumaho);
									CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(sumaho + "を使ってるんだね。いいね！"), true);
								}
							}

							// おみくじ
						}
						if (select.equals("おみくじ")) {
							// 確率をランダムで生成する
							int ranOmi = rnd.nextInt(100);

							CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("おっけー！おみくじだね！"), true);
							CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("今からおみくじを僕の中で引くね！いいものが当たるといいね"), true);
							CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ガララララララララララララララララララ"), true);

							omikuziSota(ranOmi);

							CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("おめでとう〜〜！また来年も来てね"), true);

						}
						// 会話終了
						CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("じゃあまたね"), true);

					}
				}

			}
		}
	}

	// おみくじ機能
	public static void omikuziSota(int ranOmi) {
		if (ranOmi >= 80) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("飴玉が当たったよ！"), true);
		} else if (ranOmi < 80 && ranOmi >= 95) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("うまい棒が当たったよ！"), true);
		} else if (ranOmi < 95 && ranOmi >= 100) {
			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("エンゼルパイが当たったよ！"), true);
		}
	}
}
