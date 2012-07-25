package com.github.hmdev.util;
/**
 * 文字変換と判別関連の関数定義クラス
 * Licence: Non-commercial use only.
 */
public class CharUtils
{
	/** 全角英数字を半角に変換
	 * @param src 全角文字列
	 * @return 半角文字列 */
	static public String fullToHalf(String src)
	{
		char[] c = src.toCharArray();
		for (int i=c.length-1; i>=0; i--) {
			if (c[i] >= '０' && c[i] <= '９') c[i] = (char)(c[i]-'０'+'0');
			else if (c[i] >= 'Ａ' && c[i] <= 'ｚ') c[i] = (char)(c[i]-'ａ'+'a');
		}
		return new String(c);
	}
	
	/** すべて同じ文字かチェック */
	static public boolean isSameChars(char[] ch, int begin, int end)
	{
		for (int i=begin+1; i<end; i++) {
			if (ch[begin] != ch[i]) return false;
		}
		return true;
	}
	
	/** 英字かどうかをチェック 拡張ラテン文字含む
	 * 半角スペースは含まない */
	static public boolean isHalf(char ch)
	{
		return (0x21 <= ch && ch <= 0x02AF);
	}
	
	/** 英字かどうかをチェック 拡張ラテン文字含む */
	static public boolean isAlpha(char ch)
	{
		return ('A' <= ch  && ch <= 'z' || 0x80 <= ch && ch <= 0x02AF);
	}
	
	/** 漢字かどうかをチェック
	 * 拡張領域はJavaではUTF-16の2文字になる
	 * @param pre 1文字前 なければ -1
	 * @param ch 漢字かチェックする文字
	 * @param suf 1文字後 なければ -1 */
	static public boolean isKanji(char pre, char ch, char suf)
	{
		switch (ch) {
		case '〓': return true;
		case '々': return true;
		case '〻': return true;
		case '〆': return true;
		case 'ヶ': return true;
		case 'ヵ': return true;
		}
		if (0x4E00 <= ch && ch <= 0x9FFF) return true;//'一' <= ch && ch <= '龠'
		if (0xF900 <= ch && ch <= 0xFAFF) return true;//CJK互換漢字
		//0x20000-0x2A6DF UTF-32({d840,dc00}-{d869,dedf})
		//0x2A700-0x2B81F UTF-32({d869,df00}-{d86e,dc1f})
		//0x2F800-0x2FA1F UTF-32({d87e,dc00}-{d87e,de1f})
		if (pre >= 0) {
			int code = pre<<16|ch&0xFFFF;
			if (0xD840DC00 <= code && code <= 0xD869DEDF) return true;
			if (0xD869DF00 <= code && code <= 0xD86EDC1F) return true;
			if (0xD87EDc00 <= code && code <= 0xD87EDE1F) return true;
		}
		if (suf >= 0) {
			int code = ch<<16|suf&0xFFFF;
			if (0xD840DC00 <= code && code <= 0xD869DEDF) return true;
			if (0xD869DF00 <= code && code <= 0xD86EDC1F) return true;
			if (0xD87EDc00 <= code && code <= 0xD87EDE1F) return true;
		}
		return false;
	}
	
	/** Test用 */
	/*public static void main(String[] args)
	{
		try {
			//漢字チェック
			int utf32Code = 0x2B73F;
			byte[] b = new byte[]{0, (byte)(utf32Code>>16), (byte)(utf32Code>>8), (byte)(utf32Code)};
			String s = "あ"+new String(b, "UTF-32")+"漢あああ";
			System.out.println(Integer.toHexString(s.charAt(0))+","+Integer.toHexString(s.charAt(1)));
			int length = s.length();
			for (int i=0; i<length; i++) {
				System.out.println(isKanji(i==0?(char)-1:s.charAt(i-1), s.charAt(i), i+1<length?s.charAt(i+1):(char)-1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
}