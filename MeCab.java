import java.io.*;
import java.util.*;

public class MeCab {

  public List<String> getYomiList(String str) {

    List<String> textLines = new ArrayList<String>();
    List<String> yomiList = new ArrayList<String>();

    try {
      // MeCabを起動し，入出力用のストリームを生成する
      Process process = Runtime.getRuntime().exec("mecab");
      BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
      PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())));

      pw.println(str);
      pw.flush();

      String line2;
      while ((line2 = br.readLine()) != null) {
        if (line2.equals("EOS")) {
          break;
        }
        String[] split = line2.split("[\t,]");
        String yomi = "";
        if(split.length > 8) {
          yomi = split[8];
        }
        yomiList.add(yomi);
      }
      br.close();
      pw.close();
      process.destroy();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return yomiList;
  }
}
