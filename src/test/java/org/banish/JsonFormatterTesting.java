/**
 * 
 */
package org.banish;

import java.io.IOException;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.banish.sql.core.valueformat.ValueFormatters.ValueFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * @author YY
 *
 */
public class JsonFormatterTesting implements ValueFormatter {
	
	public static Gson gson = null;
	static {
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}
	
	private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
		private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		@Override
		public void write(JsonWriter out, LocalDateTime value) throws IOException {
			if(value != null) {
				out.value(value.format(formatter));
			} else {
				out.value("");
			}
		}

		@Override
		public LocalDateTime read(JsonReader in) throws IOException {
			if(in.hasNext()) {
				String nextString = in.nextString();
				if("".equals(nextString.trim())) {
					return LocalDateTime.now();
				} else {
					return LocalDateTime.parse(nextString, formatter);
				}
			} else {
				return LocalDateTime.now();
			}
		}
	}
	
	@Override
	public String formatter() {
		return "json";
	}

	@Override
	public Object decode(Field field, String value) {
		if(List.class.isAssignableFrom(field.getType())) {
			AnnotatedType annotatedType = field.getAnnotatedType();
			ParameterizedType type = (ParameterizedType)annotatedType.getType();
			Class<?> listType = (Class<?>)type.getActualTypeArguments()[0];
			TypeToken<?> token = TypeToken.getParameterized(ArrayList.class, listType);
			return gson.fromJson(value, token.getType());
			
		} else if(ConcurrentMap.class.isAssignableFrom(field.getType())) {
			AnnotatedType annotatedType = field.getAnnotatedType();
			ParameterizedType type = (ParameterizedType)annotatedType.getType();
			Class<?> keyType = (Class<?>)type.getActualTypeArguments()[0];
			Class<?> valueType = (Class<?>)type.getActualTypeArguments()[1];
			TypeToken<?> token = TypeToken.getParameterized(ConcurrentHashMap.class, keyType, valueType);
			return gson.fromJson(value, token.getType());
			
		} else if(Map.class.isAssignableFrom(field.getType())) {
			AnnotatedType annotatedType = field.getAnnotatedType();
			ParameterizedType type = (ParameterizedType)annotatedType.getType();
			Class<?> keyType = (Class<?>)type.getActualTypeArguments()[0];
			Class<?> valueType = (Class<?>)type.getActualTypeArguments()[1];
			TypeToken<?> token = TypeToken.getParameterized(HashMap.class, keyType, valueType);
			return gson.fromJson(value, token.getType());
			
		} else if(Set.class.isAssignableFrom(field.getType())) {
			AnnotatedType annotatedType = field.getAnnotatedType();
			ParameterizedType type = (ParameterizedType)annotatedType.getType();
			Class<?> setType = (Class<?>)type.getActualTypeArguments()[0];
			TypeToken<?> token = TypeToken.getParameterized(HashSet.class, setType);
			return gson.fromJson(value, token.getType());
			
		} else {
			return gson.fromJson(value, field.getType());
		}
	}

	@Override
	public String encode(Object object) {
		return gson.toJson(object);
	}
}
