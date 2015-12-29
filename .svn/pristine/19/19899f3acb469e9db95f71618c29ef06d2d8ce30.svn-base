

package com.tuixin11sms.tx.core;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;

import com.tuixin11sms.tx.R;

public class SmileyParser {
	private static final String TAG = "SmileyParser";
	Context mContext;
	
	public static class EmotDefine
	{
		public int mRsrc;
		public String mEnc1, mEnc2;
		public String mEsc1,mEsc2,mEsc3;
		public Bitmap mBitmap;
		EmotDefine(int iResId, int _enc1,int _enc21, int _enc22, String str1, String str2, String str3)
		{
			mRsrc=iResId;
			mEnc1=_enc1>0 ? String.valueOf((char)_enc1) : null;
			if(_enc21>0){
				char chs[]={(char)_enc21,(char)_enc22};
				mEnc2=String.valueOf(chs,0,_enc22>0 ? 2 : 1);
			}else
				mEnc2=null;
			mEsc1=str1;
			mEsc2=str2;
			mEsc3=str3;
			mBitmap=null;
		}
	}
	public static EmotDefine emots[]={
		//第一页
			new EmotDefine(R.drawable.e415,0xe415,0xd83d,0xde04,"(#e415)","(#dx)",""),//笑脸
			new EmotDefine(R.drawable.e056,0xe056,0xd83d,0xde0a,"(#e056)","(#ts)",""),//开心
			new EmotDefine(R.drawable.e405,0xe405,0xd83d,0xde09,"(#e405)","(#wx)",""),//眨眼
			new EmotDefine(R.drawable.e404,0xe404,0xd83d,0xDE01,"[/露齿笑]","",""),//露齿笑
			new EmotDefine(R.drawable.e402,0xe402,0xd83d,0xDE0F,"[/得意]","",""),//得意
			new EmotDefine(R.drawable.e409,0xe409,0xd83d,0xDE1D,"[/吐舌]","",""),//吐舌
			new EmotDefine(R.drawable.e417,0xe417,0xd83d,0xDE1A,"[/接吻]","",""),//接吻
			new EmotDefine(R.drawable.e40d,0xe40d,0xd83d,0xde33,"(#e40d)","(#dy)",""),//脸红
			new EmotDefine(R.drawable.e40e,0xe40e,0xd83d,0xde12,"(#e40e)","(#hx)",""),//无语
			new EmotDefine(R.drawable.e40a,0xe40a,0xd83d,0xDE0C,"[/满意]","",""),//满意
			new EmotDefine(R.drawable.e058,0xe058,0xd83d,0xde1e,"(#e058)","(#ng)",""),//低落
			new EmotDefine(R.drawable.e715,0,0xd83d,0xde0e,"[/酷]","",""),//酷
			new EmotDefine(R.drawable.e106,0xe106,0xd83d,0xde0d,"(#e106)","(#xm)",""),//色
			new EmotDefine(R.drawable.e40c,0xe40c,0xd83d,0xde37,"(#e40c)","(#bz)",""),//生病
			new EmotDefine(R.drawable.e410,0xe410,0xd83d,0xDE32,"[/晕]","",""),	//晕
			new EmotDefine(R.drawable.e412,0xe412,0xd83d,0xDE02,"[/破泣为笑]","",""),//破泣为笑
			new EmotDefine(R.drawable.e411,0xe411,0xd83d,0xde2d,"(#e411)","(#kq)",""),//哭
			
			//第二页
			new EmotDefine(R.drawable.e753,0,0xd83d,0xde34,"[/睡觉]","",""),//睡觉
			new EmotDefine(R.drawable.e107,0xe107,0xd83d,0xde31,"(#e107)","",""),//惊恐
			new EmotDefine(R.drawable.e40b,0xe40b,0xd83d,0xde28,"(#cj)","",""),//震惊
			new EmotDefine(R.drawable.e742,0,0xd83d,0xde29,"[/叹气]","",""),//叹气
			new EmotDefine(R.drawable.e416,0xe416,0xd83d,0xDE21,"(#e416)","(#sq)",""),//生气
			new EmotDefine(R.drawable.e709,0,0,0,"[/恶魔]","",""),//恶魔
			new EmotDefine(R.drawable.e34,0,0,0,"[/鄙视]","",""),//鄙视
			new EmotDefine(R.drawable.e81,0,0,0,"[/抠鼻]","",""),//抠鼻
			new EmotDefine(R.drawable.e44,0,0,0,"[/鼓掌]","",""),//鼓掌
			new EmotDefine(R.drawable.e07,0,0,0,"[/偷笑]","",""),//偷笑
			new EmotDefine(R.drawable.e30,0,0,0,"[/抓狂]","",""),//抓狂
			new EmotDefine(R.drawable.e50,0,0,0,"[/可怜]","",""),//可怜
			new EmotDefine(R.drawable.e48,0,0,0,"[/囧]","",""),//囧
			new EmotDefine(R.drawable.e29,0,0,0,"[/再见]","",""),//再见
			new EmotDefine(R.drawable.e46,0,0,0,"[/昏迷]","",""),//昏迷
			new EmotDefine(R.drawable.e13,0,0,0,"[/大哭]","",""),//大哭
			new EmotDefine(R.drawable.e28,0,0,0,"[/疑问]","",""),//疑问
			

			//第三页
			new EmotDefine(R.drawable.e43,0,0,0,"[/无视]","",""),//无视
			new EmotDefine(R.drawable.e27,0,0,0,"[/左哼]","",""),//左哼
			new EmotDefine(R.drawable.e26,0,0,0,"[/右哼]","",""),//右哼
			new EmotDefine(R.drawable.e35,0,0,0,"[/冷汗]","",""),//冷汗
			new EmotDefine(R.drawable.e37,0,0,0,"[/瞥眼]","",""),//瞥眼
			new EmotDefine(R.drawable.e38,0,0,0,"[/衰]","",""),//衰
			new EmotDefine(R.drawable.e39,0,0,0,"[/求关注]","",""),//求关注
			new EmotDefine(R.drawable.e40,0,0,0,"[/咒骂]","",""),//咒骂
			new EmotDefine(R.drawable.e41,0,0,0,"[/拍砖]","",""),//拍砖
			new EmotDefine(R.drawable.e42,0,0,0,"[/OK]","",""),//OK
			new EmotDefine(R.drawable.e45,0,0,0,"[/握手]","",""),//握手
			new EmotDefine(R.drawable.e47,0,0,0,"[/抱拳]","",""),//抱拳
			new EmotDefine(R.drawable.e49,0,0xd83d,0xdc45,"[/舌头]","",""),//舌头
			new EmotDefine(R.drawable.e130,0xe130,0xd83c,0xdfaf,"(#e130)","",""),//飞镖
			new EmotDefine(R.drawable.e313,0xe313,0x2702,0,"(#e313)","",""),//剪刀
			new EmotDefine(R.drawable.e13b,0xe13b,0xd83d,0xdc89,"(#e13b)","",""),//打针
			new EmotDefine(R.drawable.e113,0xe113,0xd83d,0xdd2b,"(#e113)","",""),//手枪
			

			//第四页
			new EmotDefine(R.drawable.e311,0xe311,0xd83d,0xdca3,"(#e311)","",""),//炸弹
			new EmotDefine(R.drawable.e51,0,0xd83c,0xdf4c,"[/香蕉]","",""),//香蕉
			new EmotDefine(R.drawable.e305,0xe305,0xd83c,0xdf3b,"(#e305)","",""),//向日葵
			new EmotDefine(R.drawable.e032,0xe032,0xd83c,0xdf39,"(#e032)","(#e40b)",""),//玫瑰
			new EmotDefine(R.drawable.e52,0,0xd83c,0xdf51,"[/桃子]","",""),//桃子
			new EmotDefine(R.drawable.e10b,0xe10b,0xd83d,0xdc37,"(#e10b)","",""),//猪
			new EmotDefine(R.drawable.e82,0,0,0,"[/肥皂]","",""),//肥皂
			new EmotDefine(R.drawable.e53,0,0xd83c,0xdf7c,"[/奶瓶]","",""),//奶瓶
			new EmotDefine(R.drawable.e419,0xe419,0xd83d,0xdc40,"(#e419)","",""),//眼睛
			new EmotDefine(R.drawable.e11b,0xe11b,0xd83d,0xdc7b,"(#e11b)","",""),//鬼魂
			new EmotDefine(R.drawable.e201,0xe201,0xd83d,0xdeb6,"(#e201)","",""),//走
			new EmotDefine(R.drawable.e115,0xe115,0xd83c,0xDFC3,"[/跑]","",""),//跑
			new EmotDefine(R.drawable.e022,0xe022,0x2764,0,"(#e022)","",""),//心
			new EmotDefine(R.drawable.e023,0xe023,0xd83d,0xdc94,"(#e023)","",""),//心碎
			new EmotDefine(R.drawable.e12f,0xe12f,0xd83d,0xdcb0,"(#e12f)","",""),//钱
			new EmotDefine(R.drawable.e348,0xe348,0xd83c,0xdf49,"(#e348)","",""),//西瓜
			new EmotDefine(R.drawable.e30e,0xe30e,0xd83d,0xdeac,"(#e30e)","",""),//香烟
			//第五页
			new EmotDefine(R.drawable.e30f,0xe30f,0xd83d,0xdc8a,"(#e30f)","",""),//药
			new EmotDefine(R.drawable.e310,0xe310,0xd83c,0xDF88,"[/气球]","",""),//气球
			new EmotDefine(R.drawable.e00e,0xe00e,0xd83d,0xdc4d,"(#e00e)","",""),//强
			new EmotDefine(R.drawable.e421,0xe421,0xd83d,0xDC4E,"[/弱]","",""),//弱
			new EmotDefine(R.drawable.e00d,0xe00d,0xd83d,0xdc4a,"(#e00d)","",""),//拳头
			new EmotDefine(R.drawable.e011,0xe011,0x270C,0,"[/胜利]","",""),//胜利
			new EmotDefine(R.drawable.e14c,0xe14c,0xd83d,0xdcaa,"(#e14c)","",""),//强壮
			new EmotDefine(R.drawable.e22e,0xe22e,0xd83d,0xDC46,"[/上]","",""),//上
			new EmotDefine(R.drawable.e04a,0xe04a,0x2600,0,"(#e04a)","",""),//太阳
			new EmotDefine(R.drawable.e04c,0xe04c,0xd83c,0xdf19,"(#e04c)","",""),//月亮
			new EmotDefine(R.drawable.e13d,0xe13d,0x26a1,0,"(#e13d)","",""),//闪电
			new EmotDefine(R.drawable.e30c,0xe30c,0xd83c,0xdf7b,"(#e30c)","",""),//干杯
			new EmotDefine(R.drawable.e54,0,0xd83c,0xdf44,"[/蘑菇]","",""),//蘑菇
			new EmotDefine(R.drawable.e347,0xe347,0xd83c,0xDF53,"[/草莓]","",""),	//草莓
			new EmotDefine(R.drawable.e55, 0,0xd83d,0xde48,"[/捂眼]","",""),//捂眼
			new EmotDefine(R.drawable.e56,0,0xd83d,0xde49,"[/捂耳]","",""),//捂耳
			new EmotDefine(R.drawable.e57,0,0xd83d,0xde4a,"[/捂嘴]","",""),//捂嘴
			
			//第六页
			new EmotDefine(R.drawable.e448,0xe448,0xd83c,0xDF85,"[/圣诞老人]","",""),//圣诞老人
			new EmotDefine(R.drawable.e033,0xe033,0xd83c,0xDF84,"[/圣诞树]","",""),//圣诞树
			new EmotDefine(R.drawable.e58,0,0x2744,0,"[/雪花]","",""),//雪花
			new EmotDefine(R.drawable.e59, 0,0xd83c,0xdf6d,"[/棒棒糖]","",""),//棒棒糖
			new EmotDefine(R.drawable.e142,0xe142,0xd83d,0xdce2,"(#e142)","",""),//喇叭
			new EmotDefine(R.drawable.e34b,0xe34b,0xd83c,0xdf82,"(#e34b)","",""),	//生日蛋糕
			new EmotDefine(R.drawable.e445,0xe445,0xd83c,0xdf83,"(#e445)","",""),//南瓜
			new EmotDefine(R.drawable.e03c,0xe03c,0xd83c,0xdfa4,"(#e03c)","",""),	//唱歌
			new EmotDefine(R.drawable.e60, 0,0xd83d,0xdeb4,"[/骑车]","",""),//骑车
			new EmotDefine(R.drawable.e61, 0,0xd83d,0xde97,"[/小汽车]","",""),//小汽车
			new EmotDefine(R.drawable.e62, 0,0xd83d,0xde89,"[/火车站]","",""),//火车站
			new EmotDefine(R.drawable.e01d,0xe01d,0x2708,0,"[/飞机]","",""),//飞机
			new EmotDefine(R.drawable.e63, 0,0xd83d,0xdea3,"[/划船]","",""),//划船
			new EmotDefine(R.drawable.e64, 0,0xd83c,0xdfca,"[/游泳]","",""),//游泳
			new EmotDefine(R.drawable.e65, 0,0xd83c,0xdf74,"[/刀叉]","",""),//刀叉
			new EmotDefine(R.drawable.e13f,0xe13f,0xd83d,0xDEC0,"[/洗澡]","",""),//洗澡
			new EmotDefine(R.drawable.e66,0,0xd83d,0xdebd,"[/马桶]","",""),//马桶
		
			//第七页
			new EmotDefine(R.drawable.e67,0,0xd83c,0xdfbb,"[/小提琴]","",""),//小提琴
			new EmotDefine(R.drawable.e68,0,0xd83c,0xdfa8,"[/绘画]","",""),//绘画
			new EmotDefine(R.drawable.e69,0,0xd83c,0xdfae,"[/游戏机]","",""),//游戏机
			new EmotDefine(R.drawable.e70,0,0x274c,0,"[/错]","",""),//错
			new EmotDefine(R.drawable.e71,0,0x2b55,0,"[/对]","",""),//对
			new EmotDefine(R.drawable.e72,0,0x2757,0,"[/叹号]","",""),//叹号
			new EmotDefine(R.drawable.e73,0,0x2753,0,"[/问号]","",""),//问号
			new EmotDefine(R.drawable.e74, 0,0xd83d,0xdcaf,"[/百分]","",""),//百分
			new EmotDefine(R.drawable.e11d,0xe11d,0xd83d,0xDD25,"(#e11d)","",""),//火
			new EmotDefine(R.drawable.e75,0,0xd83d,0xdc8f,"[/恋爱]","",""),//恋爱
			new EmotDefine(R.drawable.e76,0,0xd83d,0xdd1e,"[/十八禁]","",""),//十八禁
			new EmotDefine(R.drawable.e77,0,0xd83c,0xde32,"[/禁止]","",""),//禁止
			new EmotDefine(R.drawable.e78,0,0xd83d,0xdebc,"[/婴儿]","",""),//婴儿
			new EmotDefine(R.drawable.e05a,0xe05a,0xd83d,0xdca9,"(#e05a)","",""),//便便
			new EmotDefine(R.drawable.e04f,0xe04f,0xd83d,0xDC31,"[/猫]","",""),//猫
			new EmotDefine(R.drawable.e052,0xe052,0xd83d,0xDC36,"[/狗头]","",""),//狗
			new EmotDefine(R.drawable.e52c,0xe52c,0xd83d,0xDC30,"[/兔子]","",""),//兔子
			//第八页
			new EmotDefine(R.drawable.e531,0xe531,0xd83d,0xDC38,"[/青蛙]","",""),//青蛙
			new EmotDefine(R.drawable.e050,0xe050,0xd83d,0xDC2F,"[/老虎]","",""),//老虎
			new EmotDefine(R.drawable.e051,0xe051,0xd83d,0xDC3B,"[/熊]","",""),//熊 
			new EmotDefine(R.drawable.e33e,0xe33e,0xd83c,0xdf5a,"(#e33e)","",""),//米饭
			new EmotDefine(R.drawable.e340,0xe340,0xd83c,0xdf5c,"(#e340)","",""),//面条
			new EmotDefine(R.drawable.e339,0xe339,0xd83c,0xdf5e,"(#e339)","",""),//面包
			new EmotDefine(R.drawable.e33a,0xe33a,0xd83c,0xdf66,"(#e33a)","",""),//冰激凌
			new EmotDefine(R.drawable.e046,0xe046,0xd83c,0xdf70,"(#e046)","",""),//蛋糕
			new EmotDefine(R.drawable.e120,0xe120,0xd83c,0xDF54,"[/汉堡]","",""),//汉堡
			new EmotDefine(R.drawable.e79,0,0xd83c,0xdd98,"[/SOS]","",""),//SOS
			new EmotDefine(R.drawable.e31c,0xe31c,0xd83d,0xdc84,"(#e31c)","",""),//口红
			new EmotDefine(R.drawable.e314,0xe314,0xd83c,0xdf80,"(#e314)","",""),//领结
			new EmotDefine(R.drawable.e306,0xe306,0xd83d,0xDC90,"[/花束]","",""),//花束
			new EmotDefine(R.drawable.e030,0xe030,0xd83c,0xDF38,"[/花]","",""),//花
			new EmotDefine(R.drawable.e80,0,0xd83d,0xdc3c,"[/熊猫]","",""),//熊猫
			
			
					};
		static EmotDefine oldemots[]={
			//兼容老的表情编码
			new EmotDefine(R.drawable.e148,0xe148,0xd83d,0xdcd6,"(#e148)","",""),
			new EmotDefine(R.drawable.e325,0xe325,0xd83d,0xdd14,"(#e325)","",""),
			new EmotDefine(R.drawable.e423,0xe423,0xd83d,0xde45,"(#e423)","",""),
			new EmotDefine(R.drawable.e426,0xe426,0xd83d,0xde47,"(#e426)","",""),
			new EmotDefine(R.drawable.e049,0xe049,0x2601,0,"(#e049)","",""),
			new EmotDefine(R.drawable.e119,0xe119,0xd83d,0xdf42,"(#e119)","",""),
			new EmotDefine(R.drawable.e140,0xe140,0xd83d,0xdebd,"(#e140)","",""),
			new EmotDefine(R.drawable.e03f,0xe03f,0xd83d,0xdd11,"(#e03f)","",""),
			new EmotDefine(R.drawable.e118,0xe118,0xd83d,0xdf41,"(#e118)","",""),
			
			//3.8.3被砍掉的表情
			new EmotDefine(R.drawable.e057,0xe057,0xd83d,0xde03,"(#e057)","",""),//
			new EmotDefine(R.drawable.e414,0xe414,0x263A,0,"[/热情]","",""),
			new EmotDefine(R.drawable.e418,0xe418,0xd83d,0xde18,"(#e418)","(#fw)",""),
			new EmotDefine(R.drawable.e105,0xe105,0xd83d,0xde1c,"(#e105)","(#gl)",""),
			new EmotDefine(R.drawable.e408,0xe408,0xd83d,0xde2a,"(#e408)","(#jj)",""),//16
			new EmotDefine(R.drawable.e403,0xe403,0xd83d,0xDE14,"[/失望]","",""),
			new EmotDefine(R.drawable.e407,0xe407,0xd83d,0xDE16,"[/呸]","",""),
			new EmotDefine(R.drawable.e406,0xe406,0xd83d,0xde23,"(#e406)","(#yy)",""),
			new EmotDefine(R.drawable.e413,0xe413,0xd83d,0xDE22,"[/眼泪]","",""),
			new EmotDefine(R.drawable.e401,0xe401,0xd83d,0xDE22,"[/焦虑]","",""),//25
			new EmotDefine(R.drawable.e410,0xe410,0xd83d,0xDE32,"[/晕]","",""),			
			new EmotDefine(R.drawable.e40f,0xe40f,0xd83d,0xDE30,"[/担心]","",""),//28			
			new EmotDefine(R.drawable.e059,0xe059,0xd83d,0xde20,"(#e059)","(#zk)",""),
			new EmotDefine(R.drawable.e108,0xe108,0xd83d,0xDE13,"[/汗]","",""),//31
			new EmotDefine(R.drawable.e11a,0xe11a,0xd83d,0xdc7f,"(#e11a)","",""),
			new EmotDefine(R.drawable.e10c,0xe10c,0xd83d,0xdc7d,"(#e10c)","",""),
			new EmotDefine(R.drawable.e329,0xe329,0xd83d,0xDC98,"[/丘比特]","",""),
			new EmotDefine(R.drawable.e32e,0xe32e,0x2728,0,"[/闪烁]","",""),
			new EmotDefine(R.drawable.e335,0xe335,0xd83c,0xdf1f,"(#e335)","",""),
			new EmotDefine(R.drawable.e03e,0xe03e,0xd83c,0xDFB5,"[/音乐]","",""),//44
			new EmotDefine(R.drawable.e22f,0xe22f,0xd83d,0xDC47,"[/下]","",""),
			new EmotDefine(R.drawable.e231,0xe231,0xd83d,0xDC49,"[/右]","",""),
			new EmotDefine(R.drawable.e230,0xe230,0xd83d,0xDC48,"[/左]","",""),
			new EmotDefine(R.drawable.e020,0xe020,0x2753,0,"[/问号]","",""),//56
			new EmotDefine(R.drawable.e04e,0xe04e,0xd83d,0xDC7C,"[/天使]","",""),
			new EmotDefine(R.drawable.e11c,0xe11c,0xd83d,0xdc80,"(#e11c)","",""),
			new EmotDefine(R.drawable.e003,0xe003,0xd83d,0xdc44,"(#e003)","",""),
			new EmotDefine(R.drawable.e021,0xe021,0x2757,0,"[/叹号]","",""),//61
			new EmotDefine(R.drawable.e04b,0xe04b,0x2614,0,"(#e04b)","",""),
			new EmotDefine(R.drawable.e048,0xe048,0x26c4,0,"(#e048)","",""),
			new EmotDefine(R.drawable.e110,0xe110,0xd83c,0xDF40,"(#e110)","",""),//69
			new EmotDefine(R.drawable.e10e,0xe10e,0xd83d,0xdc51,"(#e10e)","",""),	//72
			new EmotDefine(R.drawable.e045,0xe045,0x2615,0,"(#e045)","",""),//73
			new EmotDefine(R.drawable.e047,0xe047,0xd83c,0xdf7a,"(#e047)","",""),//74
			new EmotDefine(R.drawable.e33b,0xe33b,0xd83c,0xdf5f,"(#e33b)","",""),//77
			new EmotDefine(R.drawable.e147,0xe147,0xd83c,0xDF73,"[/煎蛋]","",""),	//81
			new EmotDefine(R.drawable.e349,0xe349,0xd83c,0xdf45,"(#e349)","",""),//85
			new EmotDefine(R.drawable.e435,0xe435,0xd83d,0xDE85,"[/火车]","",""),//89
			new EmotDefine(R.drawable.e42e,0xe42e,0xd83d,0xDE99,"[/汽车]","",""),//90
			new EmotDefine(R.drawable.e136,0xe136,0xd83d,0xDEB2,"[/自行车]","",""),//91
			new EmotDefine(R.drawable.e133,0xe133,0xd83c,0xdfb0,"(#e133)","",""),//92
			new EmotDefine(R.drawable.e13c,0xe13c,0xd83d,0xdca4,"(#e13c)","",""),//93
			new EmotDefine(R.drawable.e331,0xe331,0xd83d,0xdca6,"(#e331)","",""),//94
			new EmotDefine(R.drawable.e112,0xe112,0xd83c,0xDF81,"(#e112)","",""),
			new EmotDefine(R.drawable.e312,0xe312,0xd83c,0xDF89,"[/庆祝]","",""),
			new EmotDefine(R.drawable.e42b,0xe42b,0xd83c,0xDFC8,"[/橄榄球]","",""),
			new EmotDefine(R.drawable.e42a,0xe42a,0xd83c,0xDFC0,"[/篮球]","",""),
			new EmotDefine(R.drawable.e018,0xe018,0x26BD,0,"(#e018)","",""),
			new EmotDefine(R.drawable.e016,0xe016,0x26BE,0,"[/棒球]","",""),
			new EmotDefine(R.drawable.e014,0xe014,0x26F3,0,"[/高尔夫]","",""),
			new EmotDefine(R.drawable.e131,0xe131,0xd83c,0xDFC6,"[/奖杯]","",""),
			new EmotDefine(R.drawable.e524,0xe524,0xd83d,0xDC39,"[/仓鼠]","",""),//116
			new EmotDefine(R.drawable.e52a,0xe52a,0xd83d,0xDC3A,"[/狗]","",""),//118		
			new EmotDefine(R.drawable.e527,0xe527,0xd83d,0xDC28,"[/考拉]","",""),//121
			new EmotDefine(R.drawable.e52b,0xe52b,0xd83d,0xDC2E,"[/牛]","",""),//124
			new EmotDefine(R.drawable.e52f,0xe52f,0xd83d,0xDC17,"[/野猪]","",""),//125
			new EmotDefine(R.drawable.e01a,0xe01a,0xd83d,0xDC34,"[/马]","",""),//126
			new EmotDefine(R.drawable.e52d,0xe52d,0xd83d,0xDC0D,"[/蛇]","",""),//127
			new EmotDefine(R.drawable.e109,0xe109,0xd83d,0xDC35,"[/猴]","",""),//128
			new EmotDefine(R.drawable.e52e,0xe52e,0xd83d,0xDC14,"[/鸡]","",""),//129
			new EmotDefine(R.drawable.e055,0xe055,0xd83d,0xDC27,"[/企鹅]","",""),	//130
			new EmotDefine(R.drawable.e525,0xe525,0xd83d,0xDC1B,"[/虫子]","",""),//131
			new EmotDefine(R.drawable.e10a,0xe10a,0xd83d,0xDC19,"[/章鱼]","",""),//132
			new EmotDefine(R.drawable.e522,0xe522,0xd83d,0xDC20,"[/鱼]","",""),//133
			new EmotDefine(R.drawable.e054,0xe054,0xd83d,0xDC33,"[/鲸鱼]","",""),//134
			new EmotDefine(R.drawable.e520,0xe520,0xd83d,0xDC2C,"[/海豚]","",""),//135			
			new EmotDefine(R.drawable.e053,0xe053,0xd83d,0xDC2D,"[/老鼠]","",""),//138
			new EmotDefine(R.drawable.e123,0xe123,0x2668,0,"[/温泉]","",""),
			new EmotDefine(R.drawable.e334,0xe334,0xd83d,0xdca2,"(#e334)","",""),
			new EmotDefine(R.drawable.e443,0xe443,0xd83c,0xdf00,"(#e443)","",""),
			new EmotDefine(R.drawable.e11e,0xe11e,0xd83d,0xdcbc,"(#e11e)","",""),
			new EmotDefine(R.drawable.e11f,0xe11f,0xd83d,0xdcba,"(#e11f)","",""),
			new EmotDefine(R.drawable.e12a,0xe12a,0xd83d,0xdcfa,"(#e12a)","",""),
			new EmotDefine(R.drawable.e319,0xe319,0xd83d,0xdc57,"(#e319)","",""),
			new EmotDefine(R.drawable.e320,0xe320,0xd83d,0xdc88,"(#e320)","",""),
			new EmotDefine(R.drawable.e116,0xe116,0xd83d,0xdd28,"(#e116)","",""),
	
			
			
		};
	boolean isInTuixin;
	boolean isInChatView;
	static HashMap<String, Bitmap> _smileyToBitmap;
	static Pattern mPattern;

	public SmileyParser(Context context) {
		mContext = context.getApplicationContext();
		if (_smileyToBitmap == null)
			loadpics();
	}
	private void buildItem(StringBuilder stringbuilder, String s){
		StringBuilder stringbuilder1 = (new StringBuilder()).append("");
		String s1 = Pattern.quote(s);
		String s2 = stringbuilder1.append(s1).toString();
		stringbuilder.append(s2);
		stringbuilder.append('|');
		StringBuilder stringbuilder2 = (new StringBuilder()).append("^");
		String s3 = Pattern.quote(s);
		String s4 = stringbuilder2.append(s3).toString();
		stringbuilder.append(s4);
		stringbuilder.append('|');
	}
	private Pattern buildPattern() {
//		char c = '|';
		int i = emots.length * 5;
		StringBuilder stringbuilder = new StringBuilder(i);
		stringbuilder.append('(');
		
		for (EmotDefine emot:emots) {
			if(emot.mEnc1!=null)
				buildItem(stringbuilder,emot.mEnc1);
			if(emot.mEnc2!=null)
				buildItem(stringbuilder,emot.mEnc2);				
			if(emot.mEsc1.length()>0)
				buildItem(stringbuilder,emot.mEsc1);
			if(emot.mEsc2.length()>0)
				buildItem(stringbuilder,emot.mEsc2);
			if(emot.mEsc3.length()>0)
				buildItem(stringbuilder,emot.mEsc3);
		}
		for (EmotDefine emot:oldemots) {
			if(emot.mEnc1!=null)
				buildItem(stringbuilder,emot.mEnc1);
			if(emot.mEnc2!=null)
				buildItem(stringbuilder,emot.mEnc2);				
			if(emot.mEsc1.length()>0)
				buildItem(stringbuilder,emot.mEsc1);
			if(emot.mEsc2.length()>0)
				buildItem(stringbuilder,emot.mEsc2);
			if(emot.mEsc3.length()>0)
				buildItem(stringbuilder,emot.mEsc3);
		}

		int l = stringbuilder.length() - 1;
		int i1 = stringbuilder.length();
		stringbuilder.replace(l, i1, ")");
		return Pattern.compile(stringbuilder.toString(), 2);
	}

	public static int getDefaultSmileyResource() {
		return R.drawable.e056;
	}

	//这个方法的最后一个参数只在聊天室内使用是赋值为4，其他地方赋值都是0，它的作用是判断maxEms < 0 ，那么这个判断大部分情况为false  2014.01.22  shc
	public CharSequence addSmileySpans(CharSequence charsequence, boolean isBottom, int maxEms) {
		if (TextUtils.isEmpty(charsequence)) {
			return "";
		}
		SpannableStringBuilder spannablestringbuilder = new SpannableStringBuilder(charsequence);
		ImageSpan imagespan = null;
		for (Matcher matcher = mPattern.matcher(charsequence); matcher.find();) {
			boolean isReplaceImg = (maxEms < 0 );
			int j = matcher.start();
			int k = matcher.end();
			if (!isReplaceImg) {
				String s = matcher.group();
				Bitmap bitmap = null;
				try {
					bitmap = getSmileyResource(s);
				} catch (Exception e) {
					return spannablestringbuilder;
				}
				Matrix matrix = new Matrix();
				matrix.postScale(0.5f, 0.5f);
				Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),bitmap.getHeight(), matrix, true);
				imagespan = new ImageSpan(mContext, dstbmp, isBottom ? DynamicDrawableSpan.ALIGN_BOTTOM : DynamicDrawableSpan.ALIGN_BASELINE);
				spannablestringbuilder.setSpan(imagespan, j, k, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			} else {
				int end = k;
				if (k > spannablestringbuilder.length() - 1) {
					end = spannablestringbuilder.length() - 1;
				}
				spannablestringbuilder.replace(j, end, ".......");
			}

		}

		return spannablestringbuilder;
	}
	
	//聊天室显示上周之星用,后续修改，应该所有的addSmileySpans方法都用这个
	public CharSequence addSmileySpans(CharSequence charsequence, float height, boolean isBottom) {
		if (TextUtils.isEmpty(charsequence)) {
			return "";
		}
		SpannableStringBuilder spannablestringbuilder = new SpannableStringBuilder(charsequence);
		ImageSpan imagespan = null;
		for (Matcher matcher = mPattern.matcher(charsequence); matcher.find();) {
			int j = matcher.start();
			int k = matcher.end();
			String s = matcher.group();
			Bitmap bitmap = null;
			try {
				bitmap = getSmileyResource(s);
			} catch (Exception e) {
				return spannablestringbuilder;
			}
			int bheight = bitmap.getHeight();
			float scale = (float)height/bheight;
			Matrix matrix = new Matrix();
//			matrix.postScale(0.5f, 0.5f);
			matrix.postScale(scale, scale);
			Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),bitmap.getHeight(), matrix, true);
			imagespan = new ImageSpan(mContext, dstbmp, isBottom ? DynamicDrawableSpan.ALIGN_BOTTOM : DynamicDrawableSpan.ALIGN_BASELINE);
			spannablestringbuilder.setSpan(imagespan, j, k, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			
		}
		
		return spannablestringbuilder;
	}
	

	Pattern mP = Pattern.compile("@[\\u4e00-\\u9fa5\\w\\-.。]+");
	public CharSequence addAtpans(CharSequence charsequence) {
		if (charsequence == null) {
			return "";
		}
		SpannableString sp = new SpannableString(charsequence);
		Matcher m = mP.matcher(charsequence);
		while (m.find()) {
			sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			sp.setSpan(new ForegroundColorSpan(Color.BLACK), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		}
		return sp;
	}

	public int getSmileyResource(int i) {
		int j = getDefaultSmileyResource();
		return (i < emots.length) ? emots[i].mRsrc : j;
	}
	public Bitmap getSmileyBitmap(int i) {
		return (i < emots.length) ? emots[i].mBitmap : emots[1].mBitmap;
	}

	private Bitmap getSmileyResource(String s) {
		return (Bitmap) _smileyToBitmap.get(s);
	}

	public void loadpics() {
		Resources rss = mContext.getResources();
		//load
		mPattern = buildPattern();
		//_smileyToBitmap = buildSmileyToRes();
		_smileyToBitmap=new HashMap<String, Bitmap>();
		for(EmotDefine emot:emots){
			emot.mBitmap=((BitmapDrawable)rss.getDrawable(emot.mRsrc)).getBitmap();
			if(emot.mEnc1!=null)
				_smileyToBitmap.put(emot.mEnc1, emot.mBitmap);
			if(emot.mEnc2!=null)
				_smileyToBitmap.put(emot.mEnc2, emot.mBitmap);
			if(emot.mEsc1.length()>0)
				_smileyToBitmap.put(emot.mEsc1, emot.mBitmap);
			if(emot.mEsc2.length()>0)
				_smileyToBitmap.put(emot.mEsc2, emot.mBitmap);
			if(emot.mEsc3.length()>0)
				_smileyToBitmap.put(emot.mEsc3, emot.mBitmap);
		}
		for(EmotDefine emot:oldemots){
			emot.mBitmap=((BitmapDrawable)rss.getDrawable(emot.mRsrc)).getBitmap();
			if(emot.mEnc1!=null)
				_smileyToBitmap.put(emot.mEnc1, emot.mBitmap);
			if(emot.mEnc2!=null)
				_smileyToBitmap.put(emot.mEnc2, emot.mBitmap);
			if(emot.mEsc1.length()>0)
				_smileyToBitmap.put(emot.mEsc1, emot.mBitmap);
			if(emot.mEsc2.length()>0)
				_smileyToBitmap.put(emot.mEsc2, emot.mBitmap);
			if(emot.mEsc3.length()>0)
				_smileyToBitmap.put(emot.mEsc3, emot.mBitmap);
		}
	}

	public void setInTuixin(boolean isInTuixin) {
		this.isInTuixin = isInTuixin;
	}

	public void setInChatView(boolean isInChatView) {
		this.isInChatView = isInChatView;
	}
}






