package com.uchicom.ipm.type;

public enum Option {

	// 2) オプションフラグ種類 (command番号(32bit)のうち、上位24bit)

	IPMSG_ABSENCEOPT(0x00000100), // 不在モード（メンバ認識系コマンドで使用）
	IPMSG_SERVEROPT(0x00000200), // サーバー（予約）
	IPMSG_DIALUPOPT(0x00010000), // メンバ認識系のコマンドを個別に送り返す

	IPMSG_SENDCHECKOPT(0x00000100), // 送信チェック
	IPMSG_SECRETOPT(0x00000200), // 封書
	IPMSG_READCHECKOPT(0x00100000), // 封書確認（8 版から追加）
	IPMSG_PASSWORDOPT(0x00008000), // 錠前
	IPMSG_BROADCASTOPT(0x00000400), // ブロードキャスト（同報）
	IPMSG_MULTICASTOPT(0x00000800), // マルチキャスト（複数選択）
//	IPMSG_NEWMULTIOPT(0x00000000), // ニューバージョンマルチキャスト（予約）
	IPMSG_NOLOGOPT(0x00020000), // ログに残さない（ことを推奨）
	IPMSG_NOADDLISTOPT(0x00080000), // BR_ENTRYしていない一時メンバ通知
	IPMSG_AUTORETOPT(0x00002000), // 自動応答（ピンポン防止用）

//	IPMSG_FILEATTACHOPT(0x00000000), // ファイル添付
//	IPMSG_ENCRYPTOPT(0x00000000), // 暗号
//	IPMSG_ENCEXTMSGOPT(0x00000000), // ファイル添付情報・宛先情報を暗号文に含める

//	IPMSG_CAPUTF8OPT(0x00000000), // UTF-8を使用する能力がある
//	IPMSG_UTF8OPT(0x00000000), // メッセージ全体に UTF-8を使用している
//	IPMSG_CLIPBOARDOPT(0x00000000), // メッセージ画像埋め込み添付をサポート
//	IPMSG_CAPFILEENCOPT(0x00000000), // ファイル添付要求＆データ暗号化対応
	IPMSG_ENCFILEOPT(0x00000800), // ファイル添付データの暗号化要求
//	IPMSG_CAPIPDICTOPT(0x00000000), // IPDict形式をサポート
//	IPMSG_DIR_MASTER(0x00000000), // メンバマスター

//	IPMSG_RETRYOPT(0x00000000); // 再送フラグ（HOSTLIST 取得時に使用）
;

	private int value;

	private Option(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static Option of(int value) {
		for (Option option : values()) {
			if ((option.value & value) != 0) {
				return option;
			}
		}
		return null;
	}
}
