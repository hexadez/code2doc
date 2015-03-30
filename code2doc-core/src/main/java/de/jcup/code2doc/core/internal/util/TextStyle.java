/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.*/
package de.jcup.code2doc.core.internal.util;

import java.util.regex.Pattern;

import static de.jcup.code2doc.core.internal.util.StringUtil.*;

/**
 * Text style. This class supports simple html style mechanism.<br/>
 * <i>always need &lt;tag&gt;&lt;/tag&gt; inside never use &lt;tag/&gt;!</i>
 * <ol>
 * <li>&lti&gtcontent&lt/i&gt</li>
 * <li>&ltb&gtcontent&lt/b&gt</li>
 * <li>&ltu&gtcontent&lt/u&gt</li>
 * <li>&ltul&gtcontent&lt/ul&gt</li>
 * <li>&ltli&gtcontent&lt/li&gt</li>
 * <li>&ltp&gtcontent&lt/p&gt</li>
 * <li>&lta href='...'&gtcontent&lt/a&gt</li>
 * </ol>
 * 
 * TODO de-jcup, 22.03.2015: Think about using a parser (maybe state machine based) instead of heavy usage of regular expressions
 * 
 * @author de-jcup
 *
 */
public abstract class TextStyle {

	protected enum XHTMLReplace {
		/**
		 * Italic
		 */
		I,

		/**
		 * Bold
		 */
		B,

		/**
		 * Underline
		 */
		U,

		/**
		 * Unsorted list
		 */
		UL,

		/**
		 * List item
		 */
		LI,

		/**
		 * Paragraph
		 */
		P,

		/**
		 * New line <br/>
		 */
		BR(true),
		
		
		/**
		 * html like link Supports attribute href
		 */
		A("href");

		private final Pattern patternInternal;
		private final Pattern patternExternal;
		private final String replaceExternalWithInternal;
		private final String attribute;
		private boolean withoutContent;

		private XHTMLReplace() {
			this(null);
		}

		/**
		 * Short element variant - e.g. BR- <br/>
		 * 
		 * @param withoutContent
		 */
		private XHTMLReplace(boolean withoutContent) {
			this(null, withoutContent);
		}

		private XHTMLReplace(String attribute) {
			this(attribute, false);
		}

		private XHTMLReplace(String attribute, boolean withoutContent) {
			String tag = name().toLowerCase();
			this.attribute = attribute;
			this.withoutContent = withoutContent;
			if (this.withoutContent){
				patternExternal = Pattern.compile(patternSimpleXHTMLNoContent(tag),Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
				patternInternal = Pattern.compile(patternSimpleXHTMLNoContentToInternal(tag),Pattern.DOTALL|Pattern.CASE_INSENSITIVE);

				replaceExternalWithInternal = replacementSimpleXHTMLNoContentToInternal(tag);
			}else{
				patternExternal = Pattern.compile(patternSimpleXHTML(tag),Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
				patternInternal = Pattern.compile(patternSimpleXHTMLToInternal(tag),Pattern.DOTALL|Pattern.CASE_INSENSITIVE);

				replaceExternalWithInternal = replacementSimpleXHTMLToInternal(tag);
			}

		}
		
		private boolean isWithAttribute() {
			return isNotEmpty(attribute);
		}

		public String replace(String text, String replacement) {
			return replacePatternWith(text, patternInternal, replacement);
		}

		/* white spaces are not supported - so <u > will not work.. */
		private String patternSimpleXHTML(String element) {
			if (isWithAttribute()) {
				return "<" + element + " " + attribute + "='(.+?)'>(.+?)</" + element + ">";
			}
			return "<" + element + ">(.+?)</" + element + ">";
		}
		
		private String patternSimpleXHTMLNoContent(String element) {
			if (isWithAttribute()) {
				return "<" + element + " " + attribute + "='(.+?)'/>";
			}
			return "<" + element + "/>";
		}


		private String patternSimpleXHTMLToInternal(String element) {
			if (isWithAttribute()) {
				return createInternalString(element, "(.+?)", "(.+?)");
			}
			return createInternalString(element, "(.+?)");
		}
		
		private String patternSimpleXHTMLNoContentToInternal(String element) {
			if (isWithAttribute()) {
				return createInternalStringNoContent(element,"(.+?)");
			}
			return createInternalStringNoContent(element);
		}

		private String createInternalString(String element, String content) {
			return "\\\\\\/" + element + "\\\\\\/" + content + "\\\\\\//" + element + "\\\\\\/";
		}

		private String createInternalString(String element, String content, String attribute) {
			return "\\\\\\/" + element + "-" + attribute + "\\\\\\/" + content + "\\\\\\//" + element + "\\\\\\/";
		}
		
		private String createInternalStringNoContent(String element) {
			return "\\\\\\/" + element + "\\\\\\/";
		}

		private String createInternalStringNoContent(String element,String attribute) {
			return "\\\\\\/" + element + "-" + attribute + "\\\\\\/";
		}

		private String replacementSimpleXHTMLNoContentToInternal(String element) {
			if (isWithAttribute()) {
				return createInternalStringNoContent(element, "$1");
			}
			return createInternalStringNoContent(element);
		}
		
		private String replacementSimpleXHTMLToInternal(String element) {
			if (isWithAttribute()) {
				return createInternalString(element, "$2", "$1");
			}
			return createInternalString(element, "$1");
		}

		private String convertToInternalFormat(String text, XHTMLReplace r) {
			text = replacePatternWith(text, r.patternExternal, r.replaceExternalWithInternal);
			return text;
		}

		private String replacePatternWith(String text, Pattern p, String replacement) {
			String replaced = p.matcher(text).replaceAll(replacement);
			return replaced;
		}

	}

	/**
	 * Does apply style to given text
	 * 
	 * @param text
	 *            - source
	 * @return styled text
	 */
	public final String applyTo(String text) {
		if (isEmpty(text)) {
			return EMPTY;
		}
		String internal = convertToInternalFormat(text);
		return applyToImpl(internal);
	}

	private String convertToInternalFormat(String text) {
		for (XHTMLReplace r : XHTMLReplace.values()) {
			text = r.convertToInternalFormat(text, r);
		}
		return text;
	}

	/**
	 * Implementation of applying style. The given text is in internal format so
	 * it can be escaped in proper form (e.g. XML escaping). Simply use the
	 * XHTML replace enums inside implementation
	 * 
	 * @param internalFormatText
	 *            - is never null or empty, contains internal format
	 *            representation
	 * @return styled text
	 */
	protected abstract String applyToImpl(String internalFormatText);

	public static void main(String[] args) {
		/* javadoc output generation */
		System.out.println("* <ol>");
		for (XHTMLReplace r : XHTMLReplace.values()) {
			String name = r.name().toLowerCase();
			System.out.println("* <li>&lt" + name + (r.attribute != null ? " " + r.attribute + "='...'" : "") + "&gtcontent&lt/" + name + "&gt</li>");
		}
		System.out.println("* </ol>");
	}

}