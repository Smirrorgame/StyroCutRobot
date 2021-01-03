package robCalibration;

/* The MIT License (MIT)
 * Copyright (c) 2012 Carl Eriksson
 *  
 * Permission is hereby granted, free of charge, to any person obtaininga
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction,including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *  
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */ 

import java.util.ArrayList;

/*
 * Just a small test class that shows how the ConsoleTable class works
 */

public class ConsoleTableTester{

	public ConsoleTableTester(){
		ArrayList<String> headers = new ArrayList<String>();
		headers.add("Row");
		headers.add("Surname");
		headers.add("Lastname");
		headers.add("Age");

		ArrayList<ArrayList<String>> content = new ArrayList<ArrayList<String>>();
		ArrayList<String> row1 = new ArrayList<String>();
			row1.add("1");
			row1.add("Donald");
			row1.add("Duck");
			row1.add("55");
		ArrayList<String> row2 = new ArrayList<String>();
			row2.add("2");
			row2.add("Huey");
			row2.add("Duck");
			row2.add("13");
		ArrayList<String> row3 = new ArrayList<String>();
			row3.add("3");
			row3.add("Dewey");
			row3.add("Duck");
			row3.add("13");
		ArrayList<String> row4 = new ArrayList<String>();
			row4.add("4");
			row4.add("Louie");
			row4.add("Duck");
			row4.add("13");
		content.add(row1);
		content.add(row2);
		content.add(row3);
		content.add(row4);

		ConsoleTable ct = new ConsoleTable(headers,content);
		ct.printTable();
	}

	public static void main(String[] args) {
		new ConsoleTableTester();
	}
} 