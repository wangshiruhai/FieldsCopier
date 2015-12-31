package cov.mjp.fc;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Date;

import org.junit.Test;

public class FieldCopierTest {

	private final FieldCopier fieldCopier = FieldCopier.instance();
	private final PrimitiveNumbersHolder primitiveNumbersHolder = new PrimitiveNumbersHolder();
	private final IntegerHolder integerHolder = new IntegerHolder();
	private final ObjectHolder objectHolder = new ObjectHolder();
	
	@Test
	public void copyPrimitives() {
		ClassWithManyFields target = new ClassWithManyFields();
		fieldCopier.copyFields(primitiveNumbersHolder, target);
		assertEquals(primitiveNumbersHolder.b, target.b);
		assertEquals(primitiveNumbersHolder.d, target.d, 0d);
		assertEquals(primitiveNumbersHolder.f, target.f, 0d);
		assertEquals(primitiveNumbersHolder.i, target.i);
		assertEquals(primitiveNumbersHolder.s, target.s);
		// extra fields on the target are ignored
		assertNull(target.iObj);
		assertNull(target.ih);
		assertNull(target.unused);
	}
	
	@Test
	public void copyPrimitivesToTargetMissingFields() {
		ClassWithFewFields target = new ClassWithFewFields();
		// This will log warnings to the console (if logging is configured correctly)
		fieldCopier.copyFields(primitiveNumbersHolder, target);
		assertEquals(primitiveNumbersHolder.b, target.b);
		assertEquals(primitiveNumbersHolder.s, target.s);
	}
	
	@Test
	public void copyObjects() {
		ClassWithManyFields target1 = new ClassWithManyFields();
		fieldCopier.copyFields(integerHolder, target1);
		assertEquals(integerHolder.iObj, target1.iObj);
		// extra fields on the target are ignored
		assertNull(target1.ih);
		assertNull(target1.unused);
		// extra primitives will default to zero
		assertEquals(0, target1.b);
		assertEquals(0d, target1.d, 0d);
		assertEquals(0f, target1.f, 0d);
		assertEquals(0, target1.i);
		assertEquals(0, target1.s);

		ClassWithManyFields target2 = new ClassWithManyFields();
		fieldCopier.copyFields(objectHolder, target2);
		assertEquals(objectHolder.ih, target2.ih);
		// extra fields on the target are ignored
		assertNull(target2.iObj);
		assertNull(target2.unused);
		// extra primitives will default to zero
		assertEquals(0, target2.b);
		assertEquals(0d, target2.d, 0d);
		assertEquals(0f, target2.f, 0d);
		assertEquals(0, target2.i);
		assertEquals(0, target2.s);
	}
	
	@Test
	public void ignoresFieldsWithDifferentNames() {
		ClassWithWrongNames target = new ClassWithWrongNames();
		fieldCopier.copyFields(primitiveNumbersHolder, target);
		// 'd' is the only filed with the same name
		assertEquals(primitiveNumbersHolder.d, target.d, 0d);
		// extra primitives will default to zero
		assertEquals(0, target.b1);
		assertEquals(0f, target.f1, 0d);
		assertEquals(0, target.i1);
		assertEquals(0, target.s1);
	}
	
	@Test
	public void cannotPopulateFinalFields() {
		ClassWithFinalFields target = new ClassWithFinalFields();
		fieldCopier.copyFields(primitiveNumbersHolder, target);
		assertNotEquals(primitiveNumbersHolder.b, target.b);
		assertNotEquals(primitiveNumbersHolder.d, target.d, 0d);
		assertNotEquals(primitiveNumbersHolder.f, target.f, 0d);
		assertNotEquals(primitiveNumbersHolder.i, target.i);
		assertNotEquals(primitiveNumbersHolder.s, target.s);
	}
	
	@Test
	public void canPopulatePrivateFields() throws Exception {
		// This also demonstrates that transient fields are copied (which might not be desired).
		MyDate myDate = new MyDate();
		myDate.fastTime = 12345;
		Date date = new Date();
		fieldCopier.copyFields(myDate, date);
		Field fastTimeField = Date.class.getDeclaredField("fastTime");
		fastTimeField.setAccessible(true);
		Long fastTime = (Long) fastTimeField.get(date);
		assertEquals(myDate.fastTime, fastTime, 0d);
	}
	
	@Test
	public void superClassFieldsAreNotPopulated() throws Exception {
		SubClass target = new SubClass();
		fieldCopier.copyFields(primitiveNumbersHolder, target);
		// 'i' is the only field in the sub-class (it shadows that in the superclass) 
		assertEquals(primitiveNumbersHolder.i, target.i);
		assertEquals(0, target.b);
		assertEquals(0d, target.d, 0d);
		assertEquals(0f, target.f, 0d);
		assertEquals(0, target.s);
	}
	
//////////////////////////////////////////////////////////////////////////////////////////	

	private static class PrimitiveNumbersHolder {
		byte b = 2;
		short s = 3;
		int i = 5;
		float f = 0.3f;
		double d = 0.5;
	}

	private static class IntegerHolder {
		Integer iObj = 5;
	}

	private static class ObjectHolder {
		IntegerHolder ih = new IntegerHolder();
	}
	
	private static class ClassWithManyFields {
		byte b;
		short s;
		int i;
		float f;
		double d;
		Integer iObj;
		IntegerHolder ih;
		String unused;
	}
	
	private static class ClassWithFewFields {
		byte b;
		short s;
	}
	
	private static class ClassWithWrongNames {
		byte b1;
		short s1;
		int i1;
		float f1;
		double d;	// this is the right name
	}
	
	private static class ClassWithFinalFields {
		final byte b = 20;
		final short s = 21;
		final int i = 22;
		final float f = 23.4f;
		final double d = 25.6;
	}
	
	private static class MyDate {
		private transient long fastTime;	// transient fields are copied
	}
	
	private static class SubClass extends ClassWithManyFields {
		int i;
	}
}