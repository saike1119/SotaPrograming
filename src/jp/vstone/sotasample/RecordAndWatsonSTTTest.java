package jp.vstone.sotasample;

import java.io.File;
import java.util.List;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechAlternative;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Transcript;

import jp.vstone.RobotLib.CPlayWave;
import jp.vstone.RobotLib.CRecordMic;
import jp.vstone.RobotLib.CRobotUtil;

public class RecordAndWatsonSTTTest {

	static final String TAG = "RecordAndWatsonSTTTest";

	public static void main(String args[]){
		CRobotUtil.Log(TAG, "Start " + TAG);

		//音声ファイル録音
		CRobotUtil.Log(TAG, "Mic Recording");
		CPlayWave.PlayWave_wait("sound/start_rec_test.wav");
		CRecordMic mic = new CRecordMic();
		mic.startRecording("./test_rec.wav",5000); //5000ミリ秒（=5秒）録音
		CRobotUtil.Log(TAG, "wait end");
		mic.waitend();

		CRobotUtil.Log(TAG, "Spk Play Test");
		//音声ファイル再生
		CPlayWave.PlayWave_wait("./test_rec.wav");//確認のため，音声を再生

		SpeechToText service = new SpeechToText(); //WatsonAPIの接続口を作る
		service.setUsernameAndPassword("88088328-6c86-448d-86e7-f620d43e8c8a", "omEsuNDPVPRK");//資格情報のユーザ名とパスワードに<>も除いて置き換える

		File audio = new File("test_rec.wav");

		String model = "ja-JP_BroadbandModel"; //音声認識モデル（言語＋サンプリングレートの区分（Sotaの音声はBroadbandModelの区分））

		RecognizeOptions opts = new RecognizeOptions.Builder()
        .contentType(HttpMediaType.AUDIO_WAV) //.wavファイルの場合はAUDIO_WAV
        .continuous(true) //
        .interimResults(true) //
        .model(model) //
		.build();

		SpeechResults transcript = service.recognize(audio,opts).execute();
		//System.out.println(transcript);

		List<Transcript> t_list = transcript.getResults();
		StringBuilder t_strb = new StringBuilder();
		for(int i=0; i<t_list.size(); i++){
			SpeechAlternative elem = t_list.get(i).getAlternatives().get(0);//get(0)は「認識の第一候補を取得」の意味
			//認識の確度はelem.getConfidence()で取得できる

			String t_str = elem.getTranscript();//認識した文字列の取得
			String[] t_str_list = t_str.split(" ");
			for(int j=0;j<t_str_list.length; j++){
				if(t_str_list[j].indexOf("D_")!=0){ //先頭文字がD_（フィラーとして認識）以外のとき
					//System.out.println(t_str_list[j] + t_str_list[j].indexOf("D_"));
					t_strb.append(t_str_list[j]);
				}
				else{
					t_strb.append("、");
				}
			}
			t_strb.append("。");
		}
		String speech_text = t_strb.toString();

		System.out.println(speech_text);

	}

}
