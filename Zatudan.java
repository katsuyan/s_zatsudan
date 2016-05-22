import java.io.*;
import java.util.*;

public class Zatudan {
  private List<ReactionPattern> patternList; // 反応パターンリスト
  private List<String> questionList; // 質問パターンリスト
  private List<String> qResponseList; // 質問次のパターン
  private WeatherAPI weather = new WeatherAPI();
  private int questionNum = 0;
  private int qResponseNum = 0;


  public static void main(String[] args) {
    Zatudan instance = new Zatudan();
  }


  /*
  コンストラクタ：ここでは雑談対話システムのメインフローを記述する
  */
  public Zatudan() {
    setupReactionPattern();
    setupQuestionPattern();
    setupResponsePattern();

    String todayWeather = this.weather.getTodayWeather();

    String firstAisatsu = "";
    if(todayWeather.equals("晴れ")){
      firstAisatsu = "いい天気だね！！";
    } else {
      firstAisatsu = todayWeather + "だねー";
    }

    //最初の出力
    String output = "こんにちは！今日は" + firstAisatsu;
    System.out.println("たじー  ：" + output);

    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      System.out.print("ユーザ  ：");
      String input;
      while((input = br.readLine()) != null) {
        output = generateResponse(input, output);
        System.out.println("たじー  ：" + output);
        if(output.equals("さようなら")) {
          System.exit(0);
        }
        System.out.print("ユーザ  ：");
      }
      br.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }


  /*
  反応パターンリストの初期化を行う
  */
  public void setupReactionPattern() {
    this.patternList = new ArrayList<ReactionPattern>();
    try {
      BufferedReader br = new BufferedReader(new FileReader("pattern.txt"));
      String line;
      while ((line = br.readLine()) != null) {
        String[] split = line.split("\t");
        ReactionPattern ptn = new ReactionPattern();
        ptn.keyword = split[0];
        ptn.response = split[1];
        this.patternList.add(ptn);
      }
      br.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    ReactionPattern ptn = new ReactionPattern();
    ptn.keyword = "アシタ";
    ptn.response = "明日の天気は" + this.weather.getTommorowWeather() + "らしいよー";
    this.patternList.add(ptn);
    ptn = new ReactionPattern();
    ptn.keyword = "テンキ";
    ptn.response = "今日の天気は" + this.weather.getTodayWeather() + "明日の天気は" + this.weather.getTommorowWeather() + "みたいだよ！！";
    this.patternList.add(ptn);
  }


  /*
  質問パターンリストの初期化
  */
  public void setupQuestionPattern() {
    this.questionList = new ArrayList<String>();
    try {
      BufferedReader br = new BufferedReader(new FileReader("question.txt"));
      String line;
      while ((line = br.readLine()) != null) {
        this.questionList.add(line);
        this.questionNum += 1;
      }
      br.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /*
  反応パターンリストの初期化
  */
  public void setupResponsePattern() {
    this.qResponseList = new ArrayList<String>();
    try {
      BufferedReader br = new BufferedReader(new FileReader("response.txt"));
      String line;
      while ((line = br.readLine()) != null) {
        this.qResponseList.add(line);
        this.qResponseNum += 1;
      }
      br.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /*
  応答文を生成する
  */
  public String generateResponse(String input, String previousOutput) {
    List<ResponseCandidate> candidateList = new ArrayList<ResponseCandidate>();

    generateResponseByPattern(input, candidateList);
    generateResponseByExpansion(previousOutput, candidateList);
    generateOtherResponse(candidateList);

    // スコア最大の応答候補を選択する
    String ret = "";
    double maxScore = -1.0;
    for (ResponseCandidate cdd : candidateList) {
      if (cdd.score > maxScore) {
        ret = cdd.response;
        maxScore = cdd.score;
      }
    }
    return ret;
  }


  /*
  ①反応パターンを利用した応答候補の生成
  反応パターンはmecabを利用して「ヨミ」を利用
  */
  void generateResponseByPattern(String input, List<ResponseCandidate> candidateList) {
    double score = 3.0;

    MeCab mecab = new MeCab();
    List<String> yomiList = mecab.getYomiList(input);
    List<ReactionPattern> stub = new ArrayList<ReactionPattern>();
    for (ReactionPattern ptn : this.patternList) {
      if (yomiList.contains(ptn.keyword)) {
        ResponseCandidate cdd = new ResponseCandidate();
        cdd.response = ptn.response;
        cdd.score = score;
        candidateList.add(cdd);
        stub.add(ptn);
      }
    }
    this.patternList.removeAll(stub); //同じものが二回出力されない
  }


  /*
  ②1つ前の自分の発言からの話題展開による応答候補の生成(?前の発言の最後に?or？)
  */
  void generateResponseByExpansion(String previousOutput, List<ResponseCandidate> candidateList) {
    double score = 2.0;
    int randNum = new Random().nextInt(this.qResponseNum);
    char questionMark = previousOutput.charAt(previousOutput.length()-1);

    if(questionMark == '?' || questionMark == '？'){
      ResponseCandidate cdd = new ResponseCandidate();
      cdd.response = this.qResponseList.get(randNum);
      cdd.score = score;
      candidateList.add(cdd);
    }
  }


  /*
  ③その他の応答候補の生成(質問など)
  */
  void generateOtherResponse(List<ResponseCandidate> candidateList) {
    if(this.questionNum <= 0) { //質問仕切ったら元に戻す
      setupQuestionPattern();
    }
    double score = 1.0;
    int randNum = new Random().nextInt(this.questionNum);

    ResponseCandidate cdd = new ResponseCandidate();
    cdd.response = this.questionList.get(randNum);
    cdd.score = score;
    candidateList.add(cdd);

    questionList.remove(randNum); //一度した質問は二度しない
    this.questionNum -= 1;
  }

}
