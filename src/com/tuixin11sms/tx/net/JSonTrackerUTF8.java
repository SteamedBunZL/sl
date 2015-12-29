package com.tuixin11sms.tx.net;

public class JSonTrackerUTF8 {
	private int iCurOpenCnt;
	private int iCurSta;

	public JSonTrackerUTF8() {
		reset();
	}

	public void reset() {
		iCurOpenCnt = iCurSta = 0;
	}

	public int track(byte dat[], int iStart) {
		for (int i = iStart; i < dat.length; i++) {
			byte ch = dat[i];
			switch (iCurSta) {
			case 0:
				switch (ch) {
				case '"':
					iCurSta = 1;
					break;
				case '{':
					iCurOpenCnt++;
					break;
				case '}':
					iCurOpenCnt--;
					if (iCurOpenCnt == 0)
						return i - iStart + 1;
					else if (iCurOpenCnt < 0)
						return -1;
					break;
				}
				break;
			case 1: // 字符串中
				if (ch == (byte) '\\')
					iCurSta = 2;
				else if (ch == (byte) '"')
					iCurSta = 0;
				break;
			case 2: // 转义中
				iCurSta = 1;
				break;
			}
		}
		return 0;
	}
}
