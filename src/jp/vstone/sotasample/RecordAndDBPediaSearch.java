package jp.vstone.sotasample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechAlternative;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Transcript;

import jp.vstone.RobotLib.CPlayWave;
import jp.vstone.RobotLib.CRecordMic;
import jp.vstone.RobotLib.CRobotUtil;

public class RecordAndDBPediaSearch {

	static final String TAG = "RecordAndDBPediaSearch";
	static final String DBPEDIA_ENDPOINT = "http://ja.dbpedia.org/sparql?";
	//static final String TTS_url = "http://172.20.5.58/~hidenao/tts_voice_text.php"; //VoiceTextを実行するPHPスクリプトのURL
	static final String TTS_url = "http://133.130.107.245/tts_voice_text.php"; //VoiceTextを実行するPHPスクリプトのURL
	static final String HTTP_contentType = "application/x-www-form-urlencoded;";

	public static void main(String args[]){
		CRobotUtil.Log(TAG, "Start " + TAG);

		while(true){ //Thread.sleepで間隔をあけて繰り返し（代わりに顔認識や踊りなどを入れてもよい）

			//顔認識を入れるとしたら，このあたり？

			//音声ファイル録音
			CRobotUtil.Log(TAG, "Mic Recording");
			CPlayWave.PlayWave_wait("../sound/dbpedia_intro.wav");
			CPlayWave.PlayWave_wait("../sound/dbpedia_search_end.wav");
			CRecordMic mic = new CRecordMic();
			mic.startRecording("./dbpedia_query.wav",5000); //5000ミリ秒（=5秒）録音
			CRobotUtil.Log(TAG, "wait end");
			mic.waitend(); //設定した録音時間で録音

			CRobotUtil.Log(TAG, "Spk Play Test");
			//音声ファイル再生
			CPlayWave.PlayWave_wait("./dbpedia_query.wav");//確認のため，音声を再生

			SpeechToText service = new SpeechToText(); //WatsonAPIの接続口を作る
			//service.setUsernameAndPassword("88088328-6c86-448d-86e7-f620d43e8c8a", "omEsuNDPVPRK");//資格情報のユーザ名とパスワードに<>も除いて置き換える
			service.setUsernameAndPassword("26ae9fbc-c176-4c69-a81d-80378eca2e14", "vazWXSGPNDnz");//資格情報のユーザ名とパスワードに<>も除いて置き換える

			File audio = new File("./dbpedia_query.wav");

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

			CRobotUtil.Log(TAG, "RecognizedSpeech:"+speech_text);
			//音声コマンドによる終了（ループ脱出）
			if(speech_text.indexOf("終了")>=0){ break; }

			//「○○について教えて（ください）」と話しかけられたらDBPediaを調べる
			String subject_str = "";
			if(speech_text.indexOf("について教えて")>0){
				subject_str = speech_text.substring(0, speech_text.indexOf("について教えて"));
			}
			//「○○の△△について教えて」にするとしたら，○○をsubjectの文字列，△△をpropertyの文字列というように取得する

			CRobotUtil.Log(TAG, "SearchTerm:"+subject_str);

			//ロボットによるセリフを生成するためDBPediaを調べ，結果によってセリフを作る
			String robot_speech = "";
			if(subject_str.length()>0){
				//DBPediaに問い合わせるSPQRQLクエリを作成
				String sparql = "SELECT DISTINCT * WHERE { ";//SPQRQLを変更すれば，得られる情報を変えられる
				sparql += "<http://ja.dbpedia.org/resource/"+ subject_str+"> ";
				sparql += "<http://dbpedia.org/ontology/abstract> "; //DBPediaからとってくるデータの内容（プロパティ）はAbstractに固定
				sparql += "?object . }"; //変数objectに概要のテキストが取得される（はず）
				try{
					sparql = URLEncoder.encode(sparql,"utf-8");
				} catch (Exception e){
					e.printStackTrace();
				}

				String dbpedia_url = DBPEDIA_ENDPOINT; //SPQRQLエンドポイントにパラメータを追加
				dbpedia_url += "&query="+sparql;
				dbpedia_url += "&format=json"; //formatはこのほか，XMLやHTMLが指定可能

				CRobotUtil.Log(TAG, "DBPedia Query:"+dbpedia_url);

				String dbpedia_json_result = getStringByCallGET(dbpedia_url); //DBPediaで調べる（jsonを得る）

				//ここから：JSONからのオブジェクトの取り出し～セリフの生成までは情報源に合わせて変更する必要がある
				JsonObject json_obj = (JsonObject)new Gson().fromJson(dbpedia_json_result, JsonObject.class);
				JsonArray bindings = json_obj.get("results").getAsJsonObject()
						.get("bindings").getAsJsonArray();

				String abst_str = "";
				if(bindings.size()>0){
					abst_str = bindings.get(0).getAsJsonObject()
							.get("object").getAsJsonObject()
							.get("value").getAsString();
				}

				if(abst_str.length() > 0){ //DBPediaからの結果が得られた時のセリフの生成
					//VoiceTextを利用する場合は文字列を短くする
					if(abst_str.indexOf("。") < 140){
						abst_str = abst_str.substring(0, abst_str.indexOf("。"));
					}
					else{
						abst_str = abst_str.substring(0, 139);
					}
					robot_speech = subject_str+"を調べてみたら、「";
					robot_speech += abst_str;
					robot_speech += "」ということがわかったよ。";
				}
				else{ //DBPediaからの結果が得られなかった時のセリフの生成
					robot_speech = "ごめんね。";
					robot_speech += subject_str;
					//robot_speech += "をWikipediaで調べたけど、結果が得られませんでした。";
					robot_speech += "を調べてみたけど、わからなかったよ";
				}
				//ここまで：DBPediaの検索結果に特化した結果の処理～セリフの生成
			}
			else{ //探すべき単語が聞き取れなかったときのセリフ生成
				//robot_speech ="うまく聞き取れなくて，すみません．もう一度，尋ねますので，しばらくお待ちください．";
				robot_speech ="うまく聞き取れなかったよ．もう一度，聞くね．";

			}

			final String speech_wav_file = "./temp_speech.wav"; //話す内容として取得するファイル名
			boolean isGetWavFile = false;

			String encodeStr = "";
			try{
				encodeStr = URLEncoder.encode(robot_speech,"utf-8");
			}catch (Exception e){
				e.printStackTrace();
			}
				//emotion, emotion_level, speakerなどのパラメータを事前に用意して変更することもできる
				//テキストに対する身振りや音声・感情の調整などはここで行う

				String param ="method=post&emotion=happiness&emotion_level=2&speaker=hikari&text="+encodeStr; //VoiceTextにPOSTする各種パラメータを設定

				CRobotUtil.Log(TAG, "Converting to .wav:"+robot_speech);
				//POSTでVoice Text Web APIを利用するPHPスクリプトにアクセス（長い文字列への対応のため）
				isGetWavFile = getWavFileByCallPOST(TTS_url,HTTP_contentType, param, speech_wav_file);

				if(isGetWavFile){ //音声ファイルが取得できたら音声ファイルを再生（再生し，再生終了まで待つ）
					CPlayWave.PlayWave_wait(speech_wav_file);
				}

			try{
				Thread.sleep(10000); //ミリ秒単位でスリープ時間を指定
			} catch (Exception e){
				e.printStackTrace();
			}

		}

	}


	/*
	 * Webサーバにアクセスしてテキスト(String)をGETで取得する処理
	 * ※以下の参考サイトのコードを一部改変
	 *  http://web.plus-idea.net/2016/08/httpurlconnection-post-get-proxy-sample/
	 */
	public static String getStringByCallGET(String strGetUrl){

		HttpURLConnection con = null;
		StringBuffer result = new StringBuffer();

		try {

			URL url = new URL(strGetUrl);

			con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod("GET");
			con.connect(); //URLにGETでリクエストを送信

			// HTTPレスポンスコード
			final int status = con.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK) {
				// 通信に成功した
				// テキストを取得する
				final InputStream in = con.getInputStream();
				String encoding = con.getContentEncoding();
				if(null == encoding){
					encoding = "UTF-8";
				}
				final InputStreamReader inReader = new InputStreamReader(in, encoding);
				final BufferedReader bufReader = new BufferedReader(inReader);
				String line = null;
				// 1行ずつテキストを読み込む
				while((line = bufReader.readLine()) != null) {
					result.append(line);
					result.append("\n"); //行区切りを維持
				}
				bufReader.close();
				inReader.close();
				in.close();
			}else{
				//System.out.println(status);
			}

		}catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (con != null) {
				// コネクションを切断
				con.disconnect();
			}
		}
		//System.out.println("result=" + result.toString());

		return result.toString();
	}

	/* POSTにより音声合成サーバでWAVファイルを作成するための処理
	 *
	 */
	public static boolean getWavFileByCallPOST(String strPostUrl, String strContentType, String formParam, String target){

		HttpURLConnection con = null;

		try {

			URL url = new URL(strPostUrl);
			//System.out.println(url.toString());

			InputStream in = null;

			con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", strContentType);
			OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
			out.write(formParam);
			out.close();
			con.connect(); //URLに文字列パラメータを追加してGETでリクエストを送信

			// HTTPレスポンスコードを取得
			final int status = con.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK) { // 通信に成功した
				//バイナリファイルとしての保存
				in = con.getInputStream();
				if(in != null){
					//バイナリ形式でファイルを保存
					FileOutputStream fos = new FileOutputStream(new File(target));
					int line = -1;

					while ((line = in.read()) != -1) {
						fos.write(line);
					}
					in.close();
					fos.close();
				}

			}else{
				System.out.println(con.getResponseMessage());
				return false;
			}

		}catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (con != null) {
				// コネクションを切断
				con.disconnect();
			}
		}
		return true;
	}
}
