package jp.vstone.sotasample;

import jp.vstone.RobotLib.CPlayWave;
import jp.vstone.RobotLib.CRobotMem;
import jp.vstone.RobotLib.CRobotUtil;
import jp.vstone.RobotLib.CSotaMotion;
import jp.vstone.sotatalk.SpeechRecog;
import jp.vstone.sotatalk.TextToSpeechSota;

public class Anser {
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

			CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("お名前はなんですか？"), true);

			String name = recog.getName(15000, 3);
			if (name != null) {
				CRobotUtil.Log(TAG, name);
				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(name + "さんっていうんだね。よろしくね。"), true);

				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ちなみに焼肉と寿司、どっちが好き？"), true);
				String food = recog.getResponse(15000, 3);
				//焼肉か寿司か分岐点
				if (food != null) {
					CRobotUtil.Log(TAG, food);
					CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(name + "さんは" + food + "が好きなんだね"), true);
					//寿司ルート
					if (food.equals("寿司")) {
						CRobotUtil.Log(TAG, food);
						CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("僕もだよ、お寿司美味しいよね〜"), true);
						CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("マグロか焼肉だったらどっちが好き？"), true);
						String neta = recog.getResponse(15000, 3);
						switch (neta) {
						case "マグロ":
							CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("王道だね！"), true);
							CRobotUtil.Log(TAG, neta);
						case "ハマチ":
							CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("渋いね〜"), true);
							CRobotUtil.Log(TAG, neta);
						}
						//焼肉ルート
					}else if(food.equals("焼肉")) {
						CRobotUtil.Log(TAG, food);
						CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("焼肉いいよね"), true);
						CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("茅ヶ崎駅にはざんまいっていう美味しい焼肉屋さんがあるよ"), true);
						CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ぜひ、行ってみて！"), true);
					}else{
						CRobotUtil.Log(TAG, food);
						CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(food+"って食べ物があるの知らなかったよ、美味しそうだね。"), true);
						CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("ありがとう！"), true);
					}
				}
				CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("じゃあまたね"), true);
			}
		}
	}
}
