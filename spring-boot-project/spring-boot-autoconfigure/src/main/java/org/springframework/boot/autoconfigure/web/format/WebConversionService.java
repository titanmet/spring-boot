/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.web.format;

import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

import org.joda.time.format.DateTimeFormatterBuilder;

import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.datetime.DateFormatterRegistrar;
import org.springframework.format.datetime.joda.JodaTimeFormatterRegistrar;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.number.NumberFormatAnnotationFormatterFactory;
import org.springframework.format.number.money.CurrencyUnitFormatter;
import org.springframework.format.number.money.Jsr354NumberFormatAnnotationFormatterFactory;
import org.springframework.format.number.money.MonetaryAmountFormatter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link org.springframework.format.support.FormattingConversionService} dedicated
 * to web applications for formatting and converting values to/from the web.
 *
 * <p>This service replaces the default implementations provided by
 * {@link org.springframework.web.servlet.config.annotation.EnableWebMvc}
 * and {@link org.springframework.web.reactive.config.EnableWebFlux}.
 *
 * @author Brian Clozel
 * @since 2.0.0
 */
public class WebConversionService extends DefaultFormattingConversionService {

	private static final boolean jsr354Present = ClassUtils
			.isPresent("javax.money.MonetaryAmount", WebConversionService.class.getClassLoader());

	private static final boolean jodaTimePresent = ClassUtils
			.isPresent("org.joda.time.LocalDate", WebConversionService.class.getClassLoader());

	private String dateFormat;

	/**
	 * Create a new WebConversionService that configures formatters with the provided date format,
	 * or register the default ones if no custom format is provided.
	 * @param dateFormat the custom date format to use for date conversions
	 */
	public WebConversionService(String dateFormat) {
		super(false);
		if (StringUtils.hasText(dateFormat)) {
			this.dateFormat = dateFormat;
			addFormatters();
		}
		else {
			addDefaultFormatters(this);
		}
	}


	private void addFormatters() {
		addFormatterForFieldAnnotation(new NumberFormatAnnotationFormatterFactory());
		if (jsr354Present) {
			addFormatter(new CurrencyUnitFormatter());
			addFormatter(new MonetaryAmountFormatter());
			addFormatterForFieldAnnotation(new Jsr354NumberFormatAnnotationFormatterFactory());
		}
		registerJsr310();
		if (jodaTimePresent) {
			registerJodaTime();
		}
		registerJavaDate();
	}

	private void registerJsr310() {
		DateTimeFormatterRegistrar dateTime = new DateTimeFormatterRegistrar();
		if (this.dateFormat != null) {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter
					.ofPattern(this.dateFormat)
					.withResolverStyle(ResolverStyle.STRICT);
			dateTime.setDateFormatter(dateTimeFormatter);
		}
		dateTime.registerFormatters(this);
	}

	private void registerJodaTime() {
		JodaTimeFormatterRegistrar jodaTime = new JodaTimeFormatterRegistrar();
		if (this.dateFormat != null) {
			org.joda.time.format.DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
					.appendPattern(this.dateFormat)
					.toFormatter();
			jodaTime.setDateFormatter(dateTimeFormatter);
		}
		jodaTime.registerFormatters(this);
	}

	private void registerJavaDate() {
		DateFormatterRegistrar dateFormatterRegistrar = new DateFormatterRegistrar();
		if (this.dateFormat != null) {
			DateFormatter dateFormatter = new DateFormatter(this.dateFormat);
			dateFormatterRegistrar.setFormatter(dateFormatter);
		}
		dateFormatterRegistrar.registerFormatters(this);
	}
}