import java.io.*;
import java.net.*;
import java.util.*;

public class WeatherAPI {

  /*
  コンストラクタ
   */
  public WeatherAPI() {
  }


  /*
  今日の天気を取得する
   */
  public String getTodayWeather() {
    String tokyoXML = _getTokyoWeatherXML();

    //今日の日付取得
    Calendar cal = Calendar.getInstance();
    int now_y = cal.get(cal.YEAR);
    int now_m = cal.get(cal.MONTH) + 1;
    int now_d = cal.get(cal.DATE);


    String todayXML = "";
    String now_d_str = "";
    String now_m_str = "";
    if(now_d < 10) {
      now_d_str = "0" + now_d;
    } else {
      now_d_str = String.valueOf(now_d);
    }
    if(now_m < 10) {
      now_m_str = "0" + now_m;
    } else {
      now_m_str = String.valueOf(now_m);
    }
    if (tokyoXML != null && tokyoXML.length() > 0) {
      int offset = 0;
      while ((offset = tokyoXML.indexOf("<", offset)) != -1) {
        if (tokyoXML.startsWith(
        "<info date=\""+ now_y +"/"+ now_m_str +"/"+ now_d_str +"\">", offset)) {
          int end = tokyoXML.indexOf("</info>", offset);
          todayXML += tokyoXML.substring(offset+24, end);
        }
        offset++;
      }
    }

    String today_weather = "";
    if (todayXML != null && todayXML.length() > 0) {
      int offset = 0;
      while ((offset = todayXML.indexOf("<", offset)) != -1) {
        if (todayXML.startsWith(
        "<weather>", offset)) {
          int end = todayXML.indexOf("</weather>", offset);
          today_weather += todayXML.substring(offset+9, end);
        }
        offset++;
      }
    }
    return today_weather;
  }


  /*
  明日の天気を取得する
   */
  public String getTommorowWeather(){
    String tokyoXML = _getTokyoWeatherXML();

    //明日の日付取得
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, 1);
    int next_y = cal.get(cal.YEAR);
    int next_m = cal.get(cal.MONTH) + 1;
    int next_d = cal.get(cal.DATE);

    String tommorowXML = "";
    String next_d_str = "";
    String next_m_str = "";
    if(next_d < 10) {
      next_d_str = "0" + next_d;
    } else {
      next_d_str = String.valueOf(next_d);
    }
    if(next_m < 10) {
      next_m_str = "0" + next_m;
    } else {
      next_m_str = String.valueOf(next_m);
    }
    if (tokyoXML != null && tokyoXML.length() > 0) {
      int offset = 0;
      while ((offset = tokyoXML.indexOf("<", offset)) != -1) {
        if (tokyoXML.startsWith(
        "<info date=\""+ next_y +"/"+ next_m_str +"/"+ next_d_str +"\">", offset)) {
          int end = tokyoXML.indexOf("</info>", offset);
          tommorowXML += tokyoXML.substring(offset+24, end);
        }
        offset++;
      }
    }

    String tommorow_weather = "";
    if (tommorowXML != null && tommorowXML.length() > 0) {
      int offset = 0;
      while ((offset = tommorowXML.indexOf("<", offset)) != -1) {
        if (tommorowXML.startsWith(
        "<weather>", offset)) {
          int end = tommorowXML.indexOf("</weather>", offset);
          tommorow_weather += tommorowXML.substring(offset+9, end);
        }
        offset++;
      }
    }

    return tommorow_weather;
  }


  /*
  東京の天気XML取得
   */
  private String _getTokyoWeatherXML() {
    String weatherXML = _getWeatherXML();

    String tokyoXML = "";
    if (weatherXML != null && weatherXML.length() > 0) {
      int offset = 0;
      while ((offset = weatherXML.indexOf("<", offset)) != -1) {
        if (weatherXML.startsWith("<area id=\"東京地方\">", offset)) {
          int end = weatherXML.indexOf("</area>", offset);
          tokyoXML += weatherXML.substring(offset+16, end);
        }
        offset++;
      }
    }
    return tokyoXML;
  }


  /*
  天気XML取得
   */
  private String _getWeatherXML() {
    String response = null; // Webサーバからの応答

    // Proxyサーバの設定（学内）←設定しなくても正常動作する
    System.setProperty("http.proxyHost","proxy.sic.shibaura-it.ac.jp");
    System.setProperty("http.proxyPort","10080");

    try {
      // Web APIのリクエストURLを構築する
      String url = "http://www.drk7.jp/weather/xml/13.xml";

      // HTTP接続を確立し，処理要求を送る
      HttpURLConnection conn = (HttpURLConnection)(new URL(url)).openConnection();
      conn.setRequestMethod("GET"); // GETメソッド

      // Webサーバからの応答を受け取る
      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
      response = "";
      String line;
      while((line = br.readLine()) != null) {
        response += line;
      }
      br.close();
      conn.disconnect();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return response;
  }
}
