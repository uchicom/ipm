package com.uchicom.ipm.type;

public enum Mode {
	IPMSG_NOOPERATION(0x00000000), // 無操作
	IPMSG_BR_ENTRY(0x00000001), // サービスにエントリ（起動時にBroadcast）
	IPMSG_BR_EXIT(0x00000002), // サービスから抜ける（終了時にBroadcast）
	IPMSG_ANSENTRY(0x00000003), // エントリを認識したことを通知
	IPMSG_BR_ABSENCE(0x00000004), // 不在モード変更

	IPMSG_BR_ISGETLIST(0x00000018), // ホストリスト送出可能メンバの探索
	IPMSG_OKGETLIST(0x00000015), // ホストリスト送出可能通知
	IPMSG_GETLIST(0x00000016), // ホストリスト送出要求
	IPMSG_ANSLIST(0x00000017), // ホストリスト送出

	IPMSG_SENDMSG(0x00000020, 
			Option.IPMSG_SENDCHECKOPT, 
			Option.IPMSG_SECRETOPT, 
			Option.IPMSG_BROADCASTOPT,
			Option.IPMSG_MULTICASTOPT,
			Option.IPMSG_AUTORETOPT,
//			Option.IPMSG_RETRYOPT,
			Option.IPMSG_PASSWORDOPT,
			Option.IPMSG_NOLOGOPT,
			Option.IPMSG_NOADDLISTOPT,
			Option.IPMSG_READCHECKOPT
//			Option.IPMSG_SECRETEXOPT
			), // メッセージの送信
	IPMSG_RECVMSG(0x00000021), // メッセージの受信確認

	IPMSG_READMSG(0x00000030), // 封書の開封通知
	IPMSG_DELMSG(0x00000031), // 封書破棄通知
	IPMSG_ANSREADMSG(0x00000073), // 封書の開封確認（8 版から追加）

	IPMSG_GETFILEDATA(0x00000060), // 添付ファイル要求（TCP で使用）
	IPMSG_RELEASEFILES(0x00000061), // 添付ファイル破棄
	IPMSG_GETDIRFILES(0x00000062), // 添付階層ファイル要求

	IPMSG_GETINFO(0x00000040), // IPMSGバージョン情報取得
	IPMSG_SENDINFO(0x00000041), // IPMSGバージョン情報応答

	IPMSG_GETABSENCEINFO(0x00000050), // 不在通知文取得
	IPMSG_SENDABSENCEINFO(0x00000051), // 不在通知文応答

	IPMSG_GETPUBKEY(0x00000072), // RSA 公開鍵取得
	IPMSG_ANSPUBKEY(0x00000073), // RSA 公開鍵応答

	IPMSG_DIR_POLL(0x000000b0), // メンバマスターへのメンバ存在通知
	IPMSG_DIR_POLLAGENT(0x000000b1), // メンバマスターからのエージェント任命
	IPMSG_DIR_BROADCAST(0x000000b2), // メンバマスターからの代理ブロードキャスト依頼
	IPMSG_DIR_ANSBROAD(0x000000b3), // メンバマスターへの代理ブロードキャスト応答
	IPMSG_DIR_PACKET(0x000000b4), // メンバマスターからのメンバリスト変更通知
	IPMSG_DIR_REQUEST(0x000000b5), // メンバマスターからの代理パケット仲介依頼
	IPMSG_DIR_AGENTPACKET(0x000000b6), // エージェントからのマスター仲介パケット
;
	private int value;
	private Option[] options;

	private Mode(int value, Option...options) {
		this.value = value;
		this.options = options;
	}

	public int getValue() {
		return value;
	}

	public static Mode of(int value) {
		for (Mode mode : values()) {
			if (mode.value == (0xFF & value)) {
				return mode;
			}
		}
		return null;
	}
	public boolean is(Option option, int value) {
		for (Option tmp : options) {
			if (tmp == option) {
				if ((option.getValue() & value) == option.getValue())
				return true;
			}
		}
		return false;
	}
}
