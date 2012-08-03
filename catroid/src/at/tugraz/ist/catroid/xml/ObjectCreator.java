/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010-2011 The Catroid Team
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://www.catroid.org/catroid_license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *   
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.tugraz.ist.catroid.xml;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

import at.tugraz.ist.catroid.content.Project;
import at.tugraz.ist.catroid.content.Script;
import at.tugraz.ist.catroid.content.Sprite;

public class ObjectCreator {

	public Project getProjectWithHeaderValues(InputStream XMLFile) throws ParseException {
		HeaderTagsParser parser = new HeaderTagsParser();

		Project project = null;

		try {
			Map<String, String> headerValues = parser.parseHeader(XMLFile);
			project = Project.class.newInstance();

			Field[] projectClassFields = Project.class.getDeclaredFields();

			for (Field fieldinProject : projectClassFields) {
				boolean isCurrentFieldTransient = Modifier.isTransient(fieldinProject.getModifiers());

				if (isCurrentFieldTransient) {
					continue;
				}
				String tagName = extractTagName(fieldinProject);

				String valueInString = headerValues.get(tagName);

				if (valueInString != null) {
					Object finalObject = getObjectWithValue(fieldinProject, valueInString);
					fieldinProject.setAccessible(true);
					fieldinProject.set(project, finalObject);
				}
			}

		} catch (Throwable e) {
			throw new ParseException("Exception when creating object", e);

		}

		return project;
	}

	public Object getObjectWithValue(Field field, String valueInString) {
		String fieldClassCannonicalName = field.getType().getCanonicalName();
		if (fieldClassCannonicalName.equals("int") || fieldClassCannonicalName.equals("java.lang.Integer")) {
			return new Integer(valueInString);
		} else if (fieldClassCannonicalName.equals("java.lang.String")) {
			return valueInString;
		} else if (fieldClassCannonicalName.equals("java.lang.Double") || fieldClassCannonicalName.equals("double")) {
			return new Double(valueInString);
		} else if (fieldClassCannonicalName.equals("java.lang.Float") || fieldClassCannonicalName.equals("float")) {
			return new Float(valueInString);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Map<String, Field> getFieldMap(Class cls) {
		Map<String, Field> fieldsToSetofSuperClass = getFieldMapOfSuperClass(cls);
		Map<String, Field> fieldsToSetofClass = getFieldMapOfThisClass(cls);
		Map<String, Field> allFieldsMap = new TreeMap<String, Field>();
		allFieldsMap.putAll(fieldsToSetofClass);
		allFieldsMap.putAll(fieldsToSetofSuperClass);

		return allFieldsMap;
	}

	@SuppressWarnings("rawtypes")
	public Map<String, Field> getFieldMapOfThisClass(Class cls) {
		Map<String, Field> fieldsToSetofClass = new TreeMap<String, Field>();
		Field[] classFields = cls.getDeclaredFields();
		for (Field field : classFields) {
			boolean isCurrentFieldTransient = Modifier.isTransient(field.getModifiers());
			boolean isCurrentFieldFinal = Modifier.isFinal(field.getModifiers());
			if (isCurrentFieldTransient || isCurrentFieldFinal) {
				continue;
			}

			String tagName = extractTagName(field);

			fieldsToSetofClass.put(tagName, field);

		}
		return fieldsToSetofClass;
	}

	@SuppressWarnings("rawtypes")
	public Map<String, Field> getFieldMapOfSuperClass(Class cls) {
		Map<String, Field> fieldsToSetofSuperClass = new TreeMap<String, Field>();
		Field[] superClassFields = cls.getSuperclass().getDeclaredFields();
		for (Field field : superClassFields) {
			boolean isCurrentFieldTransient = Modifier.isTransient(field.getModifiers());
			boolean isCurrentFieldFinal = Modifier.isFinal(field.getModifiers());
			if (isCurrentFieldTransient || isCurrentFieldFinal) {
				continue;
			}

			String tagName = extractTagName(field);

			fieldsToSetofSuperClass.put(tagName, field);

		}
		return fieldsToSetofSuperClass;
	}

	@SuppressWarnings("rawtypes")
	public static Object createWithoutConstructor(final Class clazz) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		Method newInstance = ObjectInputStream.class.getDeclaredMethod("newInstance", Class.class, Class.class);
		newInstance.setAccessible(true);
		return newInstance.invoke(null, clazz, Object.class);

	}

	public String extractTagName(Field field) {
		String tagName;
		if (field.isAnnotationPresent(XMLAlias.class)) {
			XMLAlias xmlAlias = field.getAnnotation(XMLAlias.class);
			tagName = xmlAlias.value();
		} else {
			tagName = field.getName();
		}
		return tagName;
	}

	@SuppressWarnings("rawtypes")
	public Script getScriptObject(String scriptImplName, Sprite foundSprite) throws ClassNotFoundException,
			SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Script scriptObject = null;
		Class scriptClass = Class.forName("at.tugraz.ist.catroid.content." + scriptImplName);
		Constructor scriptConstructor = scriptClass.getConstructor(Sprite.class);
		if (scriptConstructor == null) {
			return (Script) getobjectOfClass(scriptClass, "0");
		}
		scriptObject = (Script) scriptConstructor.newInstance(foundSprite);

		return scriptObject;
	}

	@SuppressWarnings("rawtypes")
	public Object getobjectOfClass(Class cls, String val) throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Constructor clsConstructor = null;
		Object obj = null;
		if (cls == int.class) {
			cls = Integer.class;
		} else if (cls == float.class) {
			cls = Float.class;
		} else if (cls == double.class) {
			cls = Double.class;
		} else if (cls == boolean.class) {
			cls = Boolean.class;
		} else if (cls == byte.class) {
			cls = Byte.class;
		} else if (cls == short.class) {
			cls = Short.class;
		} else if (cls == long.class) {
			cls = Long.class;
		} else if (cls == char.class) {
			cls = Character.class;
			obj = cls.getConstructor(char.class).newInstance(val.charAt(0));
			return obj;
		} else if (cls == String.class) {
			return new String(val);
		} else {
			Method newInstance = ObjectInputStream.class.getDeclaredMethod("newInstance", Class.class, Class.class);
			newInstance.setAccessible(true);
			return newInstance.invoke(null, cls, Object.class);
		}

		clsConstructor = cls.getConstructor(String.class);
		obj = clsConstructor.newInstance(val);
		return obj;

	}

	public void setFieldOfObject(Field field, Object ObjectWithField, Object objectOfField)
			throws IllegalAccessException {
		field.setAccessible(true);
		field.set(ObjectWithField, objectOfField);
	}
}