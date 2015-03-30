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
package de.jcup.code2doc.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Element - used for all parts of API.
 * 
 * @author de-jcup
 *
 */
public abstract class Element {
	
	private static Pattern resourcePrefixPattern = Pattern.compile("\\."); 
	private Map<Locale, BaseContentContainer> mapLocaleToBaseContainer = new HashMap<Locale, Element.BaseContentContainer>();
	private String resourcePrefix;

	/**
	 * Element has only a package private constructor to avoid members outside
	 * of API.
	 */
	Element() {
		/* initialize default base container - without locale... */
		initBaseContainer(null);
	}

	/**
	 * Get a short descriptions - e.g. for a headline
	 * 
	 * @return
	 */
	public final String getHeadline() {
		return getHeadline(null);
	}

	public final String getHeadline(Locale locale) {
		return getBaseContent(locale).getHeadline();
	}

	/**
	 * Internal method to get the base content of given locale. If content not
	 * exists it will be created automatically.
	 * 
	 * @param locale
	 * @return base content - never null.
	 */
	final BaseContentContainer getBaseContent(Locale locale) {
		BaseContentContainer data = mapLocaleToBaseContainer.get(locale);
		if (data == null) {
			/* fall back */
			initBaseContainer(locale);
			data = mapLocaleToBaseContainer.get(locale);
			data.description = "FALLBACK content. There is no defined content in " + getClass().getSimpleName() + " for locale:" + locale;
		}
		return data;
	}

	/**
	 * Gets defined LOCALES - means locale the document has done a setup
	 * 
	 * @return defined LOCALES
	 */
	public final Set<Locale> getDefinedLocales() {
		return Collections.unmodifiableSet(mapLocaleToBaseContainer.keySet());
	}

	private static void notNull(String message, Object object) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}
	
	/**
	 * Prepares resource path - does convenience resource path handling - so x.y.z.Element and "test.html" will result to path "/x/y/z/test.html"
	 * @param resourcePath
	 * @return prepared path (if necessary)
	 */
	String prepareResourcePath(String resourcePath){
		notNull("resource path may not be null",  resourcePath);
		if (resourcePath.startsWith("/")){
			return resourcePath;
		}
		return getResourcePrefix()+resourcePath;
	}

	private String getResourcePrefix() {
		if (resourcePrefix==null){
			resourcePrefix= "/"+resourcePrefixPattern.matcher(getClass().getPackage().getName()).replaceAll("/")+"/";
		}
		return resourcePrefix;
	}
	

	/* internal setup base container */
	void initBaseContainer(Locale locale) {
		BaseContentContainer baseContentContainer = new BaseContentContainer(locale);
		baseContentContainer.headline = getClass().getSimpleName();
		mapLocaleToBaseContainer.put(locale, baseContentContainer);
	}

	BaseContentContainer baseContent(Locale locale) {
		BaseContentContainer content = mapLocaleToBaseContainer.get(locale);
		if (content == null) {
			initBaseContainer(locale);
			content = mapLocaleToBaseContainer.get(locale);
		}
		if (content == null) {
			throw new IllegalStateException("content may not be null at this position");
		}
		return content;
	}

	/**
	 * Immutable content.
	 * 
	 * @author de-jcup
	 *
	 */
	public interface Content {

		public List<Content> getChildren();
	}

	/**
	 * Supported resourePath languages
	 * 
	 * @author de-jcup
	 *
	 */
	public static enum CodeType {
		JAVA
	}

	/**
	 * Container for content
	 * 
	 * @author de-jcup
	 */
	public interface ContentContainer {

		/**
		 * Add styled text - supports simple html style mechanism.<br/>
		 * <b>Be aware:</b><i> always use &lt;tag&gt;&lt;/tag&gt; inside never
		 * use &lt;tag/&gt;!</i>
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
		 * @param text
		 * @return current container
		 */
		public ContentContainer addText(String text);

		/**
		 * Add styled text - supports simple html style mechanism.<br/>
		 * <b>Be aware:</b><i> always use &lt;tag&gt;&lt;/tag&gt; inside never
		 * use &lt;tag/&gt;!</i>
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
		 * @param resourcePath
		 *            - path to a resource file containing text. if resource
		 *            path starts not with a / the element package path will be
		 *            added automatically before.
		 * @return current container
		 */
		public ContentContainer addTextResource(String resourcePath);

		/**
		 * Add resourePath
		 * 
		 * @param codeType
		 *            type of resourePath
		 * @param resourcePath
		 * @return current container
		 */
		public ContentContainer addCode(CodeType codeType, String code);

		/**
		 * Add java resourePath from resource
		 * 
		 * @param codeType
		 *            type of resourePath
		 * @param resourcePath
		 *            - if resource path starts not with a / the element package
		 *            path will be added automatically before.
		 * @return current container
		 */
		public ContentContainer addCodeResource(CodeType codeType, String resourcePath);

		/**
		 * Add a picture
		 * 
		 * @param resourcePath
		 *            - if resource path starts not with a / the element package
		 *            path will be added automatically before.
		 * @return current container
		 */
		public ContentContainer addPictureResource(String title, String resourcePath);

		/**
		 * Open a new headline container
		 * 
		 * @param headline
		 * @return new headline container
		 */
		public ContentContainer openHeadlineContainer(String headline);

		/**
		 * Close current container and return to parent
		 * 
		 * @return parent container
		 * @throws IllegalStateException
		 *             when no parent is available
		 */
		public ContentContainer closeContainer();

	}

	public class CodeContent extends NoChildrenContent {
		private String code;
		private String language;

		private CodeContent(CodeType codeType, String code) {
			notNull("CodeType may not be null", codeType);
			notNull("Code may not be null", code);
			this.code = code;
			this.language = codeType.name().toLowerCase();
		}

		public String getCode() {
			return code;
		}

		public String getLanguage() {
			return language;
		}
	}

	public class CodeContentResource extends NoChildrenContent {
		private String resourcePath;
		private String language;

		private CodeContentResource(CodeType codeType, String resourcePath) {
			notNull("codeType may not be null", codeType);
			notNull("resourcePath may not be null", resourcePath);
			this.resourcePath = resourcePath;
			this.language = codeType.name().toLowerCase();
		}

		public String getResourcePath() {
			return resourcePath;
		}

		public String getLanguage() {
			return language;
		}
	}

	public class TextContent extends NoChildrenContent {
		private String text;

		private TextContent(String text) {
			notNull("text may not be null", text);
			this.text = text;
		}

		public String getText() {
			return text;
		}

	}

	public class TextContentResource extends NoChildrenContent {
		private String resourcePath;

		private TextContentResource(String resourcePath) {
			notNull("resourcePath may not be null", resourcePath);
			this.resourcePath = resourcePath;
		}

		public String getResourcePath() {
			return resourcePath;
		}

	}

	public class PictureContentResource extends NoChildrenContent {
		private String resourcePathToPicture;
		private String title;

		private PictureContentResource(String title, String resourcePathToPicture) {
			notNull("title may not be null", title);
			notNull("resourcePathToPicture may not be null", resourcePathToPicture);
			this.title = title;
			this.resourcePathToPicture = resourcePathToPicture;
		}

		public String getTitle() {
			return title;
		}

		public String getResourcePath() {
			return resourcePathToPicture;
		}

	}

	public class LinkToURLContent extends NoChildrenContent {
		private String url;
		private String message;

		private LinkToURLContent(String message, String url) {
			notNull("message may not be null", message);
			notNull("url may not be null", url);
			this.message = message;
			this.url = url;
		}

		public String getMessage() {
			return message;
		}

		public String getUrl() {
			return url;
		}

	}

	public class HeadlineContainer extends AbstractContentContainer {
		private String headline;

		public HeadlineContainer(String headline) {
			notNull("headline may not be null", headline);
			this.headline = headline;
		}

		public String getHeadline() {
			return headline;
		}

	}

	/**
	 * Abstract container implementation
	 * 
	 * @author de-jcup
	 *
	 */
	class AbstractContentContainer implements Content, ContentContainer {

		ContentContainer parent;
		private List<Content> contentData = new ArrayList<Content>();

		@Override
		public ContentContainer addCode(CodeType codeType, String code) {
			contentData.add(new CodeContent(codeType, code));
			return this;
		}

		@Override
		public ContentContainer addCodeResource(CodeType codeType, String resourcePath) {
			notNull("codeType may not be null", codeType);
			notNull("resourcePath may not be null", resourcePath);
			contentData.add(new CodeContentResource(codeType, prepareResourcePath(resourcePath)));
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.jcup.code2doc.api.ContentContainer#addText(java.lang.String)
		 */
		@Override
		public ContentContainer addText(String text) {
			notNull("text may not be null", text);
			contentData.add(new TextContent(text));
			return this;
		}

		@Override
		public ContentContainer addTextResource(String resourcePath) {
			notNull("resourcePath may not be null", resourcePath);
			contentData.add(new TextContentResource(prepareResourcePath(resourcePath)));
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.jcup.code2doc.api.ContentContainer#addPicture(java.lang.String)
		 */
		@Override
		public ContentContainer addPictureResource(String title, String resourcePath) {
			notNull("title may not be null", title);
			notNull("resourcePath may not be null", resourcePath);
			contentData.add(new PictureContentResource(title, prepareResourcePath(resourcePath)));
			return this;
		}

		@Deprecated
		/* to be removed we have <a href now... */
		public ContentContainer addLinkToURL(String text, String url) {
			notNull("text may not be null", text);
			notNull("url may not be null", url);
			contentData.add(new LinkToURLContent(text, url));
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.jcup.code2doc.api.ContentContainer#openHeadlineContainer(java.lang
		 * .String)
		 */
		@Override
		public ContentContainer openHeadlineContainer(String headline) {
			notNull("headline may not be null", headline);
			HeadlineContainer headlineContainer = new HeadlineContainer(headline);
			headlineContainer.parent = this;
			contentData.add(headlineContainer);
			return headlineContainer;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.jcup.code2doc.api.ContentContainer#closeContainer()
		 */
		@Override
		public ContentContainer closeContainer() {
			if (parent == null) {
				throw new IllegalStateException("cannot close this container because end reached!");
			}
			return parent;
		}

		@Override
		public List<Content> getChildren() {
			return Collections.unmodifiableList(contentData);
		}

	}

	class BaseContentContainer extends AbstractContentContainer {
		String description = "";
		String headline = "";
		Locale locale;

		private BaseContentContainer(Locale locale) {
			this.locale = locale;
		}

		String getHeadline() {
			return headline;
		}

		String getDescription() {
			return description;
		}

		Locale getLocale() {
			return locale;
		}
	}

	class NoChildrenContent implements Content {

		@Override
		public final List<Content> getChildren() {
			return Collections.emptyList();
		}

	}

}