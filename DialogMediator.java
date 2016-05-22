import java.io.*;

// 対話システムどうしで対話を行うためのプログラム
// コマンド行の引数として，対話プログラムのパスを2つ指定する
// その他に指定可能なオプション：
// -color　対話をカラー表示する
// -until 発言回数　指定した回数の発言を行った後，終了する
// -wait 秒数　発言と発言の間の待ち時間（デフォルトは1）
// 例： java DialogueMediator -color -until 100 -wait 3 program1 program2

public class DialogMediator {
    // インスタンス変数
    boolean color = false; // デフォルトは白黒
    int until = 0; // デフォルトは自動終了なし
    double waitTime = 1.0; // デフォルトは1秒
    String[] colleaguePath = new String[2]; // 対話プログラムのパス
    String[] nickname = new String[2]; // 対話プログラムの呼び名

    // メインメソッド
    public static void main(String[] args) {
	DialogMediator instance = new DialogMediator(args);
    }

    // コンストラクタ：ここにメインフローを記述する
    public DialogMediator(String[] args) {
	// コマンド行オプションの解析
	analyzeOptions(args);

	// プロセス間通信のための変数
	Process[] process = new Process[2];
	BufferedReader[] br = new BufferedReader[2];
	PrintWriter[] pw = new PrintWriter[2];

	try {
	    // 対話プログラムを起動する
	    for (int i = 0; i < 2; i++) {
		process[i] = Runtime.getRuntime().exec(colleaguePath[i]);
		br[i] = new BufferedReader(
				new InputStreamReader(process[i].getInputStream()));
		pw[i] = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(process[i].getOutputStream())));
	    }

	    // 対話プログラム２の最初の挨拶を読み捨てる．呼び名を取得する
	    String line = br[1].readLine();
	    if (line == null) {
		System.out.println(colleaguePath[1] + "から行が読めません");
	    }
	    int colon = line.indexOf('：');
	    if (colon == -1) {
		System.out.println("全角コロン「：」がありません");
	    }
	    nickname[1] = line.substring(0, colon);

	    // 対話開始
	    int turn = 0; // 対話プログラム１から始める
	    int count = 0; // 発言回数のカウンタ
	    while (true) {
		line = br[turn].readLine(); // 発言内容を受信する
		if (line == null) {
		    System.out.println(colleaguePath[turn] + "が終了しました");
		    break;
		}
		colon = line.indexOf('：');
		if (colon == -1) {
		    System.out.println("全角コロン「：」がありません");
		}
		String text;
		if (count == 0) { // 対話プログラム１の最初の発言のみ
		    nickname[turn] = line.substring(0, colon); // 呼び名を取得
		    text = line.substring(colon+1); // 発言内容
		} else {
		    colon = line.indexOf('：', colon+1); // 「ユーザ：」を読み飛ばすため
		    text = line.substring(colon+1);
		}
		printText(turn, nickname[turn], text); // 発言内容の出力

		turn = 1 - turn; // 話者交代
		pw[turn].println(text); // 発言内容を相手に送信する
		pw[turn].flush();
		count++;
		if (count == until) {
		    break;
		}
		Thread.sleep((long)(waitTime * 1000.0)); // waitTime秒，待つ
	    }
	    for (int i = 0; i < 2; i++) { // 後始末を行う
		br[i].close();
		pw[i].close();
		process[i].destroy();
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

    }

    // コマンド行オプションの解析
    public void analyzeOptions(String[] args) {
	int turn = 0;

	for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("-color")) {
		color = true;
	    } else if (args[i].equals("-until")) {
		until = Integer.parseInt(args[++i]);
	    } else if (args[i].equals("-wait")) {
		waitTime = Double.parseDouble(args[++i]);
	    } else if (turn < 2) {
		String path = args[i]; // 対話プログラムのパス
		if (new File(path).exists()) { // ファイルの存在確認
		    colleaguePath[turn] = path;
		    turn++;
		} else {
		    System.out.println("can't find " + path);
		}
	    }
	}
	if (turn < 2) {
	    System.out.println("対話プログラムのパスを２つ指定して下さい");
	}
    }

    // 発言を出力する
    public void printText(int turn, String nickname, String text) {
	String[] colorCode = { "\033[32m", "\033[33m" };

	if (color) {
	    System.out.print(colorCode[turn]);
	}
	System.out.println(nickname + "：" + text);
    }

}
