package jp.vstone.sotasample;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import jp.vstone.RobotLib.CPlayWave;

public class VoiceTextTTSServerTest {

	public static void main(String[] args) {


		final String wav_file = "./temp.wav"; //話す内容として取得するファイル名
		boolean isGetWavFile = false;

		try{
			//しゃべる内容をサーバ側のテキストファイルから取得
			//String getText_url = "http://172.20.5.58/~b4p31047/temp.txt"; //.txt以外にPHPやRubyなどでもOK（一行でUTF-8のテキストが返ってくれば，なんでも可）
			String getText_url = "http://133.130.107.245/temp.txt"; //.txt以外にPHPやRubyなどでもOK（一行でUTF-8のテキストが返ってくれば，なんでも可）
			String speech_text = "テキスト取得，エラーです";

			speech_text = getStringByCallGET(getText_url); //ローカルでのテキスト取得も可能ですが，Sota側の計算能力は貧弱です

			//String TTS_url = "http://172.20.5.58/~hidenao/tts_voice_text.php"; //VoiceTextを実行するPHPスクリプトのURL
			String TTS_url = "http://133.130.107.245/tts_voice_text.php"; //VoiceTextを実行するPHPスクリプトのURL
			//String HTTP_contentType = "application/x-www-form-urlencoded;";
			String encodeStr = URLEncoder.encode(speech_text,"utf-8");

			//emotion, emotion_level, speakerなどのパラメータを事前に用意して変更することもできる

			String param ="method=post&emotion=happiness&emotion_level=2&speaker=hikari&text="+encodeStr; //VoiceTextにPOSTする各種パラメータを設定
			//GETでVoice Text Web APIを利用するPHPスクリプトにアクセス（通常はリクエスト文字列長8KBまでのためPOSTへの変更も可能）
			isGetWavFile = getWavFileByCallGET(TTS_url,param,wav_file);

		}catch (Exception e){
			e.printStackTrace();
		}

		if(isGetWavFile){ //音声ファイルが取得できたら音声ファイルを再生（再生し，再生終了まで待つ）
			CPlayWave.PlayWave_wait(wav_file);
		}
	}

	/* POSTにより音声合成サーバでWAVファイルを作成するための処理
	 *
	 */
	public static boolean getWavFileByCallGET(String strPostUrl, String param, String target){

		HttpURLConnection con = null;

		try {

			URL url = new URL(strPostUrl+"?"+param);
			//System.out.println(url.toString());

			InputStream in = null;

			con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("GET");

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




}


