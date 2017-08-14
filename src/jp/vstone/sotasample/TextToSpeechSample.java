package jp.vstone.sotasample;

import jp.vstone.RobotLib.CPlayWave;
import jp.vstone.RobotLib.CRobotUtil;
import jp.vstone.sotatalk.TextToSpeechSota;

public class TextToSpeechSample {
	static final String TAG = "SpeechRecSample";
	public static void main(String[] args) {
		//CPlayWave.PlayWave(TextToSpeechSota.getTTS("やっほー"),true);
		//CPlayWave.PlayWave(TextToSpeechSota.getTTS("僕の名前はSotaです。"),true);
		byte[] data = TextToSpeechSota.getTTS("そろそろ、定時だよ！");
		if(data == null){
			CRobotUtil.Log(TAG,"ERROR");
		}
		CPlayWave.PlayWave(data,true);

		CPlayWave.PlayWave(TextToSpeechSota.getTTS("お疲れ様でした！"),true);
		//CPlayWave.PlayWave(TextToSpeechSota.getTTS("リンくん、彼女作りましょう！"),true);

	}
}
