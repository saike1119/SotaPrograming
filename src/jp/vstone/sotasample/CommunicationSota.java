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

					CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(hello + ",そーたです！今からお話しようよ。"), true);
					CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("まず、あなたのお名前はなんていうの？"), true);

					// 話題の番号をランダムで生成する
					Random rnd = new Random();
					int ran = rnd.nextInt(3);

					String name = recog.getName(15000, 3);
					if (name != null) {
						CRobotUtil.Log(TAG, name);
						CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(name + "さんっていうんだね。よろしくね。"), true);

						if (ran == 0) {
							CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ちなみに焼肉と寿司、どっちが好き？"), true);
							String food = recog.getResponse(15000, 3);
							// 焼肉か寿司か分岐点
							if (food != null) {
								CRobotUtil.Log(TAG, food);
								CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(name + "さんは" + food + "が好きなんだね"), true);
								// 寿司ルート
								if (food.equals("寿司") || food.equals("おすし")) {
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
									CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(food + "って食べ物があるの知らなかったよ、美味しそうだね。"),
											true);
									CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ありがとう！"), true);
								}
							}
						}
						// TODO:開発途中の話題
						if (ran == 1) {
							CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("最近の芸能人って可愛い人が多いよね、誰が好きとかある？"), true);

							String geinou = recog.getResponse(15000, 3);
							if (geinou != null) {
								CRobotUtil.Log(TAG, geinou);
								CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(geinou + "さんが好きなんだね。いいね！。"), true);
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
						// 会話終了
						CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("じゃあまたね"), true);
					}
				}
			}
		}
	}
}
