package jk.springframework.context.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;
import org.springframework.util.StringUtils;

/**
 * {@link org.springframework.context.support.ReloadableResourceBundleMessageSource} <br>
 * 순수 소스에서 호출된 code를 알아보기 위해 {@link #hitMessageCodeMap}을 추가
 * @author kamoru
 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
 *
 */
@Slf4j
public class ReloadableResourceBundleMessageSource extends
		AbstractMessageSource implements ResourceLoaderAware {

	private static final String PROPERTIES_SUFFIX = ".properties";

	private static final String XML_SUFFIX = ".xml";

	private String[] basenames = new String[0];

	private String defaultEncoding;

	private Properties fileEncodings;

	private boolean fallbackToSystemLocale = true;

	private long cacheMillis = -1;

	private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	/** Cache to hold filename lists per Locale */
	private final Map<String, Map<Locale, List<String>>> cachedFilenames = new HashMap<String, Map<Locale, List<String>>>();

	/** Cache to hold already loaded properties per filename */
	private final Map<String, PropertiesHolder> cachedProperties = new HashMap<String, PropertiesHolder>();

	/** Cache to hold merged loaded properties per locale */
	private final Map<Locale, PropertiesHolder> cachedMergedProperties = new HashMap<Locale, PropertiesHolder>();

	/** hit한 message 정보. [code_locale, value] */
	public static Map<String, String> hitMessageCodeMap = new TreeMap<String, String>();

	/**
	 * Set a single basename, following the basic ResourceBundle convention of
	 * not specifying file extension or language codes, but in contrast to
	 * {@link ResourceBundleMessageSource} referring to a Spring resource
	 * location: e.g. "WEB-INF/messages" for "WEB-INF/messages.properties",
	 * "WEB-INF/messages_en.properties", etc.
	 * <p>
	 * XML properties files are also supported: .g. "WEB-INF/messages" will find
	 * and load "WEB-INF/messages.xml", "WEB-INF/messages_en.xml", etc as well.
	 * 
	 * @param basename
	 *            the single basename
	 * @see #setBasenames
	 * @see org.springframework.core.io.ResourceEditor
	 * @see java.util.ResourceBundle
	 */
	public void setBasename(String basename) {
		setBasenames(new String[] { basename });
	}

	/**
	 * Set an array of basenames, each following the basic ResourceBundle
	 * convention of not specifying file extension or language codes, but in
	 * contrast to {@link ResourceBundleMessageSource} referring to a Spring
	 * resource location: e.g. "WEB-INF/messages" for
	 * "WEB-INF/messages.properties", "WEB-INF/messages_en.properties", etc.
	 * <p>
	 * XML properties files are also supported: .g. "WEB-INF/messages" will find
	 * and load "WEB-INF/messages.xml", "WEB-INF/messages_en.xml", etc as well.
	 * <p>
	 * The associated resource bundles will be checked sequentially when
	 * resolving a message code. Note that message definitions in a
	 * <i>previous</i> resource bundle will override ones in a later bundle, due
	 * to the sequential lookup.
	 * 
	 * @param basenames
	 *            an array of basenames
	 * @see #setBasename
	 * @see java.util.ResourceBundle
	 */
	public void setBasenames(String[] basenames) {
		if (basenames != null) {
			this.basenames = new String[basenames.length];
			for (int i = 0; i < basenames.length; i++) {
				String basename = basenames[i];
				Assert.hasText(basename, "Basename must not be empty");
				this.basenames[i] = basename.trim();
			}
		} else {
			this.basenames = new String[0];
		}
	}

	/**
	 * Set the default charset to use for parsing properties files. Used if no
	 * file-specific charset is specified for a file.
	 * <p>
	 * Default is none, using the <code>java.util.Properties</code> default
	 * encoding.
	 * <p>
	 * Only applies to classic properties files, not to XML files.
	 * 
	 * @param defaultEncoding
	 *            the default charset
	 * @see #setFileEncodings
	 * @see org.springframework.util.PropertiesPersister#load
	 */
	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	/**
	 * Set per-file charsets to use for parsing properties files.
	 * <p>
	 * Only applies to classic properties files, not to XML files.
	 * 
	 * @param fileEncodings
	 *            Properties with filenames as keys and charset names as values.
	 *            Filenames have to match the basename syntax, with optional
	 *            locale-specific appendices: e.g. "WEB-INF/messages" or
	 *            "WEB-INF/messages_en".
	 * @see #setBasenames
	 * @see org.springframework.util.PropertiesPersister#load
	 */
	public void setFileEncodings(Properties fileEncodings) {
		this.fileEncodings = fileEncodings;
	}

	/**
	 * Set whether to fall back to the system Locale if no files for a specific
	 * Locale have been found. Default is "true"; if this is turned off, the
	 * only fallback will be the default file (e.g. "messages.properties" for
	 * basename "messages").
	 * <p>
	 * Falling back to the system Locale is the default behavior of
	 * <code>java.util.ResourceBundle</code>. However, this is often not
	 * desirable in an application server environment, where the system Locale
	 * is not relevant to the application at all: Set this flag to "false" in
	 * such a scenario.
	 */
	public void setFallbackToSystemLocale(boolean fallbackToSystemLocale) {
		this.fallbackToSystemLocale = fallbackToSystemLocale;
	}

	/**
	 * Set the number of seconds to cache loaded properties files.
	 * <ul>
	 * <li>Default is "-1", indicating to cache forever (just like
	 * <code>java.util.ResourceBundle</code>).
	 * <li>A positive number will cache loaded properties files for the given
	 * number of seconds. This is essentially the interval between refresh
	 * checks. Note that a refresh attempt will first check the last-modified
	 * timestamp of the file before actually reloading it; so if files don't
	 * change, this interval can be set rather low, as refresh attempts will not
	 * actually reload.
	 * <li>A value of "0" will check the last-modified timestamp of the file on
	 * every message access. <b>Do not use this in a production environment!</b>
	 * </ul>
	 */
	public void setCacheSeconds(int cacheSeconds) {
		this.cacheMillis = (cacheSeconds * 1000);
	}

	/**
	 * Set the PropertiesPersister to use for parsing properties files.
	 * <p>
	 * The default is a DefaultPropertiesPersister.
	 * 
	 * @see org.springframework.util.DefaultPropertiesPersister
	 */
	public void setPropertiesPersister(PropertiesPersister propertiesPersister) {
		this.propertiesPersister = (propertiesPersister != null ? propertiesPersister
				: new DefaultPropertiesPersister());
	}

	/**
	 * Set the ResourceLoader to use for loading bundle properties files.
	 * <p>
	 * The default is a DefaultResourceLoader. Will get overridden by the
	 * ApplicationContext if running in a context, as it implements the
	 * ResourceLoaderAware interface. Can be manually overridden when running
	 * outside of an ApplicationContext.
	 * 
	 * @see org.springframework.core.io.DefaultResourceLoader
	 * @see org.springframework.context.ResourceLoaderAware
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = (resourceLoader != null ? resourceLoader
				: new DefaultResourceLoader());
	}

	/**
	 * Resolves the given message code as key in the retrieved bundle files,
	 * returning the value found in the bundle as-is (without MessageFormat
	 * parsing).
	 */
	@Override
	protected String resolveCodeWithoutArguments(String code, Locale locale) {
		if (this.cacheMillis < 0) {
			PropertiesHolder propHolder = getMergedProperties(locale);
			String result = propHolder.getProperty(code);
			if (result != null) {
				hitMessageCodeMap.put(code + "_" + locale, result);
				log.trace("code={}, locale={} value={} cacheMillis={}", code, locale, result, cacheMillis);
				return result;
			}
		} else {
			for (String basename : this.basenames) {
				List<String> filenames = calculateAllFilenames(basename, locale);
				log.trace("basename={} locale={} filenames={}", basename, locale, filenames);
				for (String filename : filenames) {
					PropertiesHolder propHolder = getProperties(filename);
					String result = propHolder.getProperty(code);
					if (result != null) {
						hitMessageCodeMap.put(code + "_" + locale, result);
						log.trace("code={}, locale={} value={} filename={}", code, locale, result, filename);
						return result;
					}
				}
			}
		}
		hitMessageCodeMap.put(code + "_" + locale, null);
		log.trace("code={}, locale={} value=null", code, locale);
		return null;
	}
	
	/**
	 * Resolves the given message code as key in the retrieved bundle files,
	 * using a cached MessageFormat instance per message code.
	 */
	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		if (this.cacheMillis < 0) {
			PropertiesHolder propHolder = getMergedProperties(locale);
			MessageFormat result = propHolder.getMessageFormat(code, locale);
			if (result != null) {
				hitMessageCodeMap.put(code + "_" + locale, result.toString());
				log.trace("code={}, locale={} value={}", code, locale, result);
				return result;
			}
		} else {
			for (String basename : this.basenames) {
				List<String> filenames = calculateAllFilenames(basename, locale);
				for (String filename : filenames) {
					PropertiesHolder propHolder = getProperties(filename);
					MessageFormat result = propHolder.getMessageFormat(code,
							locale);
					if (result != null) {
						hitMessageCodeMap.put(code + "_" + locale, result.toPattern());
						log.trace("code={}, locale={} value={}", code, locale, result);
						return result;
					}
				}
			}
		}
		hitMessageCodeMap.put(code + "_" + locale, null);
		log.trace("code={}, locale={} value=null", code, locale);
		return null;
	}

	/**
	 * Get a PropertiesHolder that contains the actually visible properties for
	 * a Locale, after merging all specified resource bundles. Either fetches
	 * the holder from the cache or freshly loads it.
	 * <p>
	 * Only used when caching resource bundle contents forever, i.e. with
	 * cacheSeconds < 0. Therefore, merged properties are always cached forever.
	 */
	@SuppressWarnings("rawtypes")
	protected PropertiesHolder getMergedProperties(Locale locale) {
		synchronized (this.cachedMergedProperties) {
			PropertiesHolder mergedHolder = this.cachedMergedProperties
					.get(locale);
			if (mergedHolder != null) {
				return mergedHolder;
			}
			Properties mergedProps = new Properties();
			mergedHolder = new PropertiesHolder(mergedProps, -1);
			for (int i = this.basenames.length - 1; i >= 0; i--) {
				List filenames = calculateAllFilenames(this.basenames[i], locale);
				for (int j = filenames.size() - 1; j >= 0; j--) {
					String filename = (String) filenames.get(j);
					PropertiesHolder propHolder = getProperties(filename);
					if (propHolder.getProperties() != null) {
						mergedProps.putAll(propHolder.getProperties());
					}
				}
			}
			this.cachedMergedProperties.put(locale, mergedHolder);
			return mergedHolder;
		}
	}

	/**
	 * Calculate all filenames for the given bundle basename and Locale. Will
	 * calculate filenames for the given Locale, the system Locale (if
	 * applicable), and the default file.
	 * 
	 * @param basename
	 *            the basename of the bundle
	 * @param locale
	 *            the locale
	 * @return the List of filenames to check
	 * @see #setFallbackToSystemLocale
	 * @see #calculateFilenamesForLocale
	 */
	protected List<String> calculateAllFilenames(String basename, Locale locale) {
		log.trace("basename={}, locale={}", basename, locale);
		synchronized (this.cachedFilenames) {
			Map<Locale, List<String>> localeMap = this.cachedFilenames.get(basename);
			if (localeMap != null) {
				List<String> filenames = localeMap.get(locale);
				log.trace("found in localeMap - filenames={}", filenames);
				if (filenames != null) {
					return filenames;
				}
			}
			List<String> filenames = new ArrayList<String>(7);
			filenames.addAll(calculateFilenamesForLocale(basename, locale));
			log.trace("calculate filenames={}", filenames);
			log.trace("fallbackToSystemLocale={}, Locale.getDefault={} ", filenames, Locale.getDefault());
			if (this.fallbackToSystemLocale && !locale.equals(Locale.getDefault())) {
				List<String> fallbackFilenames = calculateFilenamesForLocale(basename, Locale.getDefault());
				log.trace("fallback Filenames={}", fallbackFilenames);
				for (String fallbackFilename : fallbackFilenames) {
					if (!filenames.contains(fallbackFilename)) {
						// Entry for fallback locale that isn't already in
						// filenames list.
						filenames.add(fallbackFilename);
						log.trace("add fallback basename - fallbackFilename={}", fallbackFilename);
					}
				}
			}
			filenames.add(basename);
			log.trace("add default basename - basename={}", basename);
			if (localeMap != null) {
				localeMap.put(locale, filenames);
			} else {
				localeMap = new HashMap<Locale, List<String>>();
				localeMap.put(locale, filenames);
				this.cachedFilenames.put(basename, localeMap);
			}
			log.trace("return filenames={}", filenames);
			return filenames;
		}
	}

	/**
	 * Calculate the filenames for the given bundle basename and Locale,
	 * appending language code, country code, and variant code. E.g.: basename
	 * "messages", Locale "de_AT_oo" -> "messages_de_AT_OO", "messages_de_AT",
	 * "messages_de".
	 * <p>
	 * Follows the rules defined by {@link java.util.Locale#toString()}.
	 * 
	 * @param basename
	 *            the basename of the bundle
	 * @param locale
	 *            the locale
	 * @return the List of filenames to check
	 */
	protected List<String> calculateFilenamesForLocale(String basename, Locale locale) {
		List<String> result = new ArrayList<String>(3);
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		StringBuilder temp = new StringBuilder(basename);

		temp.append('_');
		if (language.length() > 0) {
			temp.append(language);
			result.add(0, temp.toString());
			log.trace("1 {}", temp);
		}

		temp.append('_');
		if (country.length() > 0) {
			temp.append(country);
			result.add(0, temp.toString());
			log.trace("2 {}", temp);
		}

		if (variant.length() > 0
				&& (language.length() > 0 || country.length() > 0)) {
			temp.append('_').append(variant);
			result.add(0, temp.toString());
			log.trace("3 {}", temp);
		}
		log.trace("result {}", result);
		return result;
	}

	/**
	 * Get a PropertiesHolder for the given filename, either from the cache or
	 * freshly loaded.
	 * 
	 * @param filename
	 *            the bundle filename (basename + Locale)
	 * @return the current PropertiesHolder for the bundle
	 */
	protected PropertiesHolder getProperties(String filename) {
		synchronized (this.cachedProperties) {
			PropertiesHolder propHolder = this.cachedProperties.get(filename);
			if (propHolder != null
					&& (propHolder.getRefreshTimestamp() < 0 
							|| propHolder.getRefreshTimestamp() > System.currentTimeMillis() - this.cacheMillis)) {
				// up to date
				return propHolder;
			}
			return refreshProperties(filename, propHolder);
		}
	}

	/**
	 * Refresh the PropertiesHolder for the given bundle filename. The holder
	 * can be <code>null</code> if not cached before, or a timed-out cache entry
	 * (potentially getting re-validated against the current last-modified
	 * timestamp).
	 * 
	 * @param filename
	 *            the bundle filename (basename + Locale)
	 * @param propHolder
	 *            the current PropertiesHolder for the bundle
	 */
	protected PropertiesHolder refreshProperties(String filename, PropertiesHolder propHolder) {
		long refreshTimestamp = (this.cacheMillis < 0) ? -1 : System.currentTimeMillis();

		Resource resource = this.resourceLoader.getResource(filename + PROPERTIES_SUFFIX);
		if (!resource.exists()) {
			resource = this.resourceLoader.getResource(filename + XML_SUFFIX);
		}

		if (resource.exists()) {
			long fileTimestamp = -1;
			if (this.cacheMillis >= 0) {
				// Last-modified timestamp of file will just be read if caching
				// with timeout.
				try {
					fileTimestamp = resource.lastModified();
					if (propHolder != null
							&& propHolder.getFileTimestamp() == fileTimestamp) {
//						if (log.isDebugEnabled()) {
//							log.debug("Re-caching properties for filename ["
//									+ filename
//									+ "] - file hasn't been modified");
//						}
						propHolder.setRefreshTimestamp(refreshTimestamp);
						return propHolder;
					}
				} catch (IOException ex) {
					// Probably a class path resource: cache it forever.
					if (log.isDebugEnabled()) {
						log.debug(resource + " could not be resolved in the file system - assuming that is hasn't changed", ex);
					}
					fileTimestamp = -1;
				}
			}
			try {
				Properties props = loadProperties(resource, filename);
				propHolder = new PropertiesHolder(props, fileTimestamp);
			} catch (IOException ex) {
				if (log.isWarnEnabled()) {
					log.warn("Could not parse properties file [" + resource.getFilename() + "]", ex);
				}
				// Empty holder representing "not valid".
				propHolder = new PropertiesHolder();
			}
		}

		else {
			// Resource does not exist.
//			if (log.isDebugEnabled()) {
//				log.debug("No properties file found for [" + filename
//						+ "] - neither plain properties nor XML");
//			}
			// Empty holder representing "not found".
			propHolder = new PropertiesHolder();
		}

		propHolder.setRefreshTimestamp(refreshTimestamp);
		this.cachedProperties.put(filename, propHolder);
		return propHolder;
	}

	/**
	 * Load the properties from the given resource.
	 * 
	 * @param resource
	 *            the resource to load from
	 * @param filename
	 *            the original bundle filename (basename + Locale)
	 * @return the populated Properties instance
	 * @throws IOException
	 *             if properties loading failed
	 */
	protected Properties loadProperties(Resource resource, String filename) throws IOException {
		InputStream is = resource.getInputStream();
		Properties props = new Properties();
		try {
			if (resource.getFilename().endsWith(XML_SUFFIX)) {
				if (log.isDebugEnabled()) {
					log.debug("Loading properties [" + resource.getFilename() + "]");
				}
				this.propertiesPersister.loadFromXml(props, is);
			} else {
				String encoding = null;
				if (this.fileEncodings != null) {
					encoding = this.fileEncodings.getProperty(filename);
				}
				if (encoding == null) {
					encoding = this.defaultEncoding;
				}
				if (encoding != null) {
					if (log.isDebugEnabled()) {
						log.debug("Loading properties [" + resource.getFilename() + "] with encoding '" + encoding + "'");
					}
					this.propertiesPersister.load(props, new InputStreamReader(is, encoding));
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Loading properties [" + resource.getFilename() + "]");
					}
					this.propertiesPersister.load(props, is);
				}
			}
			return props;
		} finally {
			is.close();
		}
	}

	/**
	 * Clear the resource bundle cache. Subsequent resolve calls will lead to
	 * reloading of the properties files.
	 */
	public void clearCache() {
		log.debug("Clearing entire resource bundle cache");
		synchronized (this.cachedProperties) {
			this.cachedProperties.clear();
		}
		synchronized (this.cachedMergedProperties) {
			this.cachedMergedProperties.clear();
		}
	}

	/**
	 * Clear the resource bundle caches of this MessageSource and all its
	 * ancestors.
	 * 
	 * @see #clearCache
	 */
	public void clearCacheIncludingAncestors() {
		clearCache();
		if (getParentMessageSource() instanceof ReloadableResourceBundleMessageSource) {
			((ReloadableResourceBundleMessageSource) getParentMessageSource())
					.clearCacheIncludingAncestors();
		}
	}

	@Override
	public String toString() {
		return getClass().getName() + ": basenames=["
				+ StringUtils.arrayToCommaDelimitedString(this.basenames) + "]";
	}

	/**
	 * PropertiesHolder for caching. Stores the last-modified timestamp of the
	 * source file for efficient change detection, and the timestamp of the last
	 * refresh attempt (updated every time the cache entry gets re-validated).
	 */
	protected class PropertiesHolder {

		private Properties properties;

		private long fileTimestamp = -1;

		private long refreshTimestamp = -1;

		/** Cache to hold already generated MessageFormats per message code */
		private final Map<String, Map<Locale, MessageFormat>> cachedMessageFormats = new HashMap<String, Map<Locale, MessageFormat>>();

		public PropertiesHolder(Properties properties, long fileTimestamp) {
			this.properties = properties;
			this.fileTimestamp = fileTimestamp;
		}

		public PropertiesHolder() {
		}

		public Properties getProperties() {
			return properties;
		}

		public long getFileTimestamp() {
			return fileTimestamp;
		}

		public void setRefreshTimestamp(long refreshTimestamp) {
			this.refreshTimestamp = refreshTimestamp;
		}

		public long getRefreshTimestamp() {
			return refreshTimestamp;
		}

		public String getProperty(String code) {
			if (this.properties == null) {
				return null;
			}
			return this.properties.getProperty(code);
		}

		public MessageFormat getMessageFormat(String code, Locale locale) {
			if (this.properties == null) {
				return null;
			}
			synchronized (this.cachedMessageFormats) {
				Map<Locale, MessageFormat> localeMap = this.cachedMessageFormats
						.get(code);
				if (localeMap != null) {
					MessageFormat result = localeMap.get(locale);
					if (result != null) {
						return result;
					}
				}
				String msg = this.properties.getProperty(code);
				if (msg != null) {
					if (localeMap == null) {
						localeMap = new HashMap<Locale, MessageFormat>();
						this.cachedMessageFormats.put(code, localeMap);
					}
					MessageFormat result = createMessageFormat(msg, locale);
					localeMap.put(locale, result);
					return result;
				}
				return null;
			}
		}
	}

}
