php-serialize-kt
---

This is serializer/deserializer of [PHP serialize](https://www.php.net/manual/en/function.serialize.php) for Kotlin and Java.

## Description

In this library, we provide PHP primitive types and Array, Object wrapped data types and the code to serialize/unserialize with those data types from PHP serialization to each other.

## Getting Started

### Dependencies

* Java 8 or over

### Installing

TODO: write after push to repository

### Usage

#### Serialize

from Kotlin

```kotlin
import com.jsoizo.phpserialize.*

val serializer = Serializer()

val pArray = pArrayOf(
    PInt(0) to PString("zero"),
    PInt(1) to PString("one")
)
val serializedArray = serializer.serialize(pArray)
// a:2:{i:0;s:4:"zero";i:1;s:3:"one";}

val pObject = PObject(
    name = "ExampleClass", 
    mapOf(
        "field1" to PInt(42),
        "field2" to PString("hello")
    )
)
val serializedObject = serializer.serialize(pObject)
// O:12:"ExampleClass":2:{s:6:"field1";i:42;s:6:"field2";s:5:"hello";}
```

from Java

```java
import com.jsoizo.phpserialize.*;

import java.util.LinkedHashMap;

LinkedHashMap<PArrayKey, PValue> arrayValue = new LinkedHashMap<>();
arrayValue.put(new PInt(0), new PString("zero"));
arrayValue.put(new PInt(1), new PString("one"));
PArray pArray = new PArray(arrayValue);
String serializedArray = serializer.serialize(pArray);

LinkedHashMap<String, PValue> objectValue = new LinkedHashMap<>();
objectValue.put("field1", new PInt(42));
objectValue.put("field2", new PString("hello"));
PObject pObject = new PObject("ExampleClass", objectValue);
String serializedObject = serializer.serialize(pObject);
```

#### Unserialize


from Kotlin

```kotlin
import com.jsoizo.phpserialize.*

val unserializer = Unserializer()

val serializedArray = "a:2:{i:1;s:3:\"one\";i:2;s:3:\"two\";}"
val unserializedArray = unserializer.unserialize(serializedArray)
// PArray(value={PInt(value=0)=PString(value=zero), PInt(value=1)=PString(value=one)})


val serializedObject = "O:12:\"ExampleClass\":2:{s:6:\"field1\";i:42;s:6:\"field2\";s:5:\"hello\";}"
val unserializedObject = unserializer.unserialize(serializedObject)
// PObject(name=ExampleClass, value={field1=PInt(value=42), field2=PString(value=hello)})
```

from Java

```java
import com.jsoizo.phpserialize.*;

Unserializer unserializer = new Unserializer();

String serializedArray = "a:2:{i:1;s:3:\"one\";i:2;s:3:\"two\";}";
PValue unserializedArray = unserializer.unserialize(serializedArray);

String serializedObject = "O:12:\"ExampleClass\":2:{s:6:\"field1\";i:42;s:6:\"field2\";s:5:\"hello\";}";
PValue unserializedObject = unserializer.unserialize(serializedObject);
```

## License

This project is licensed under the @jsoizo License - see the LICENSE.md file for details
