/*
 * Copyright (c) 2016 Kayler Renslow
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * The software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement. in no event shall the authors or copyright holders be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the software or the use or other dealings in the software.
 */

package com.kaylerrenslow.armaDialogCreator.gui.fx.main.controlPropertiesEditor;

import com.kaylerrenslow.armaDialogCreator.control.sv.SVInteger;
import com.kaylerrenslow.armaDialogCreator.gui.fx.control.inputfield.InputFieldDataChecker;
import com.kaylerrenslow.armaDialogCreator.gui.fx.control.inputfield.IntegerChecker;
import org.jetbrains.annotations.NotNull;

/**
 Checker for Integers that returns a SerializableValue

 @author Kayler
 @since 05/31/2016. */
public class SVIntegerChecker implements InputFieldDataChecker<SVInteger> {
	private static final IntegerChecker checker = new IntegerChecker();

	@Override
	public String validData(@NotNull String data) {
		return checker.validData(data);
	}

	@Override
	public SVInteger parse(@NotNull String data) {
		Integer i = checker.parse(data);
		if (i == null) {
			throw new IllegalStateException("returned value shouldn't be null");
		}
		return new SVInteger(i);
	}

	@Override
	public String getTypeName() {
		return checker.getTypeName();
	}

	@Override
	public boolean allowEmptyData() {
		return checker.allowEmptyData();
	}
}
