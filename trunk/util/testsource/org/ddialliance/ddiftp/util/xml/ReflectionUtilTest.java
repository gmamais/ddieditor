package org.ddialliance.ddiftp.util.xml;

import junit.framework.Assert;

import org.ddialliance.ddiftp.util.ReflectionUtil;
import org.junit.Test;

/*
* Copyright 2011 Danish Data Archive (http://www.dda.dk) 
* 
* This program is free software; you can redistribute it and/or modify it 
* under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either Version 3 of the License, or 
* (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful, 
* but WITHOUT ANY WARRANTY; without even the implied warranty of 
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*  
* You should have received a copy of the GNU Lesser General Public 
* License along with this library; if not, write to the 
* Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
* Boston, MA  02110-1301  USA
* The full text of the license is also available on the Internet at 
* http://www.gnu.org/copyleft/lesser.html
*/

public class ReflectionUtilTest {

	public interface TestInterface {
		public String getTest();

		public String setTest(String test);
	}

	public class TestImpl implements TestInterface {
		String test;

		public TestImpl() {
		}

		public TestImpl(String test) {
			this.test = test;
		}

		public String getTest() {
			return test;
		}

		public String setTest(String test) {
			this.test = test;
			return test;
		}
	}

	public class Example {
		private TestInterface test;

		public TestInterface getTest() {
			return test;
		}

		public void setTest(TestInterface test) {
			this.test = test;
		}
	}

	@Test
	public void invokeMethod() throws Exception {
		Example example = new Example();
		TestImpl testImpl = new TestImpl("test");
		try {
			ReflectionUtil.invokeMethod(example, "setTest", false, testImpl);
			Assert.fail();
		} catch (NoSuchMethodException e) {
			// TODO: handle exception
		}

		ReflectionUtil.invokeMethod(example, "setTest", true, testImpl);
		Assert.assertEquals(testImpl,
				ReflectionUtil.invokeMethod(example, "getTest", false));
	}
}
