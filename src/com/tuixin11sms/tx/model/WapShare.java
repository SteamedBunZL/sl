package com.tuixin11sms.tx.model;

import android.content.Context;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.message.TXMessage;

/**
 * 神聊(转发、分享)的内容
 * 
 * @author webster
 * 
 */
public class WapShare {
	private Context context;
	/** 文本类型 */
	public static final int TYPE_TEXT = 0;
	/** 图片类型 */
	public static final int TYPE_IMAGE = 1;
	/** 音频类型 */
	public static final int TYPE_AUDIO = 2;
	/** 要分享的类型 */
	private int type;
	/** 要发送给Server的内容 */
	private String content;
	/** 分享内容的序列号（唯一标识） */
	private String sn;
	/** 分享对应的Txmessage */
	private TXMessage txMsg;
	/** 对应的wap页面的地址 */
	private String url;
	/** 转发给其他人时用得主题 */
	private String subject;
	/** 转发给其他人时用得Text */
	private String text;
	/** sns类型 */
	private int snsType;

	public WapShare() {

	}

	public WapShare(TXMessage txMsg) {
		if (txMsg != null) {
			this.txMsg = txMsg;
			this.sn = String.valueOf(txMsg.msg_id);
			switch (txMsg.msg_type) {
			case TxDB.MSG_TYPE_COMMON_SMS:
			case TxDB.MSG_TYPE_QU_COMMON_SMS: {
				type = TYPE_TEXT;
				content = txMsg.msg_body;
				break;
			}
			case TxDB.MSG_TYPE_IMAGE_EMS:
			case TxDB.MSG_TYPE_QU_IMAGE_EMS: {
				type = TYPE_IMAGE;
				content = getMutiMediaContent(txMsg);
				break;
			}
			case TxDB.MSG_TYPE_AUDIO_EMS:
			case TxDB.MSG_TYPE_QU_AUDIO_EMS:
				type = TYPE_AUDIO;
				content = getMutiMediaContent(txMsg);
				break;
			}
		}
	}

	private String getMutiMediaContent(TXMessage txMsg) {
		if (txMsg.msg_url != null) {
			String[] strs = txMsg.msg_url.split(":");
			if (strs != null && strs.length >= 3) {
				if (type == TYPE_IMAGE)
					return strs[2];
				else if (type == TYPE_AUDIO)
					return strs[2].concat(":").concat(
							String.valueOf(txMsg.audio_times));
			}
		}
		return null;
	}

	public int getType() {
		return type;
	}

	public String getContent() {
		return content;
	}

	public String getSn() {
		return sn;
	}

	public String getUrl() {
		if (url == null)
			url = "";
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
//		switch (type) {
//		case WapShare.TYPE_TEXT: {
//			text = String.format(context.getString(R.string.share_text),
//					txMsg.msg_body, TX.tm.getTxMe().partner_id);
//			subject = String.format(context.getString(R.string.share_subject),
//					"一段神奇文字");
//			break;
//		}
//		case WapShare.TYPE_IMAGE: {
//			text = String.format(context.getString(R.string.share_image), url,
//					TX.tm.getTxMe().partner_id);
//			subject = String.format(context.getString(R.string.share_subject),
//					"一张神奇图片");
//			break;
//		}
//		case WapShare.TYPE_AUDIO: {
//			text = String.format(context.getString(R.string.share_audio), url,
//					TX.tm.getTxMe().partner_id);
//			subject = String.format(context.getString(R.string.share_subject),
//					"一张神奇音频");
//			break;
//		}
		//}
	}

	public TXMessage getTxMsg() {
		return txMsg;
	}

	public String getSubject() {
		return subject;
	}

	public String getText() {
		return text;
	}

	/**
	 * 3.8.4新加需求，zjx
	 * @param snsType
	 *            sns类型，1.微博，2其它
	 * @return
	 */
	public String getSnsText(int snsType) {
		switch (type) {
		case WapShare.TYPE_TEXT: {
			switch (snsType) {
			case 1: {
				text = String.format(
						context.getString(R.string.weibo_share_text),
						txMsg.msg_body);
				subject = String.format(
						context.getString(R.string.share_subject), "文字");
				break;
			}
			case 3: {
				//邮件的分享文字
				text = String.format(
						context.getString(R.string.email_share_text),
						txMsg.msg_body);
				subject = String.format(
						context.getString(R.string.share_subject), "文字");
				break;
			}

			default: {
				text = String.format(context.getString(R.string.share_text),
						txMsg.msg_body, TX.tm.getTxMe().partner_id);
				subject = String.format(
						context.getString(R.string.share_subject), "文字");
				break;
			}

			}

			break;
		}
		case WapShare.TYPE_IMAGE: {
			switch (snsType) {
			case 1: {
				text = String.format(
						context.getString(R.string.weibo_share_image), url);
				subject = String.format(
						context.getString(R.string.share_subject), "图片");

				break;
			}
			default: {
				text = String.format(context.getString(R.string.share_image),
						url, TX.tm.getTxMe().partner_id);
				subject = String.format(
						context.getString(R.string.share_subject), "图片");
				break;

			}

			}

			break;
		}
		case WapShare.TYPE_AUDIO: {

			switch (snsType) {
			case 1: {
				text = String.format(
						context.getString(R.string.weibo_share_audio), url);
				subject = String.format(
						context.getString(R.string.share_subject), "音频");
				break;
			}

			default: {
				text = String.format(context.getString(R.string.share_audio),
						url, TX.tm.getTxMe().partner_id);
				subject = String.format(
						context.getString(R.string.share_subject), "音频");
				break;
			}
			}

			break;
		}
		}
		return text;
	}

	public void setContext(Context context) {
		this.context = context;
	}

}
