package jp.vstone.sotasample;

import java.awt.Color;

import jp.vstone.RobotLib.CPlayWave;
import jp.vstone.RobotLib.CRobotMem;
import jp.vstone.RobotLib.CRobotPose;
import jp.vstone.RobotLib.CRobotUtil;
import jp.vstone.RobotLib.CSotaMotion;
import jp.vstone.sotatalk.TextToSpeechSota;

public class Senden {
	static final String TAG = "SpeechRecSample";

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
			pose.SetPose(new Byte[] { 1, 2, 3, 4, 5 }, new Short[] { 0, -900, 0, 900, 0 });
			// LEDを点灯（左目：赤、右目：赤、口：Max、電源ボタン：赤）
			pose.setLED_Sota(Color.BLUE, Color.BLUE, 255, Color.BLUE);

			motion.play(pose, 100);
			CRobotUtil.wait(100);

			// 右手を挙げるポーズ
			pose.SetPose(new Byte[] { 1, 2, 3, 4, 5 }, new Short[] { 0, -900, 0, -800, 0 });
			motion.play(pose, 1000);
			CRobotUtil.wait(100);
			while (true) {
				try {
					Thread.sleep(3000);
					rndHelloSota();
					pose = new CRobotPose();
					pose.SetPose(new Byte[] { 1, 2, 3, 4, 5 }, new Short[] { 0, -900, 0, 900, 0 });
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
		}
	}

	// functions
	public static void rndHelloSota() {
		CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("こんにちは〜〜！こちらぬるつーブースです。ゆっくりしていってね！"), true);
	}
}
