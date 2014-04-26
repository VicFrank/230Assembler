import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class Assembler {
	//uses JRE 1.7

	public static void main(String[] args) {
		Integer count = 0;
		
		File file = new File("AssembledMemory.mif");
		FileWriter fw = null;
		if (!file.exists()) {
			try {
				file.createNewFile();
				fw = new FileWriter(file.getAbsoluteFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BufferedWriter bw = new BufferedWriter(fw);
		try {
			bw.write("WIDTH=24;");
			bw.newLine();
			bw.write("DEPTH=1024;");
			bw.newLine();
			bw.write("ADDRESS_RADIX=UNS;");
			bw.newLine();
			bw.write("DATA_RADIX=HEX;");
			bw.newLine();
			bw.write("CONTENT BEGIN;");
			bw.newLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		BufferedReader br = null;

	    String line;
		try {
			br = new BufferedReader(new FileReader(args[0]));
			while ((line = br.readLine()) != null){
				String instruction1 = null;
				String instruction2 = null;
				String instruction3 = null;
				String cond = null;
				String command = null;
				String[] result = new String[6];
				//parse line by line
				command = line.split("[ ]+")[0];
				command = command.toUpperCase();
				//cut out the command and the whitespace
				line = line.substring(command.length(), line.length()-1);
				line = line.trim();
				//now get the comma delimited instructions
				String[] instructions = line.split("[,]");
				instruction1 = instructions[0];
				if(instructions.length == 2){
					instruction2 = instructions[1];
				}
				else if(instructions.length == 3){
					instruction3 = instructions[2];
				}
				int setBit = 0;
				//break down command if it has an s bit
				if(command.startsWith("S")){
					setBit = 1;
					command = command.substring(1);
				}
				//break down the command if it has a cond
				if(command.length() > 3 || (command.startsWith("B") && !command.equals("BAL"))){
					cond = command.substring(command.length()-2, command.length()-1);
					command = command.substring(0, command.length()-3);
				}
				//now convert into hex
				Integer opCode = null;
				Integer opx = null;
				Integer condValue = null;
				String type = null;
				switch (command) {
				case "ADD": opCode = 0; opx = 3; type = "R"; break;
				case "SUB": opCode = 0; opx = 4; type = "R"; break;
				case "AND": opCode = 0; opx = 0; type = "R"; break;
				case "OR":  opCode = 0; opx = 1; type = "R"; break;
				case "XOR": opCode = 0; opx = 2; type = "R"; break;
				case "SLL": opCode = 1; opx = 0; type = "R"; break;
				case "CMP": opCode = 2; opx = 0; type = "R"; setBit = 1; break;
				case "JR":  opCode = 3; opx = 0; type = "R"; break;
				
				case "LW":  opCode = 4; type = "D"; break;
				case "SW":  opCode = 6; type = "D"; break;
				case "ADDI":opCode = 5; type = "D"; break;
				case "SI":  opCode = 7; type = "D"; break;
				
				case "B":   opCode = 8; type = "B"; break;
				case "BAL": opCode = 9; type = "B"; break;
				
				case "J":   opCode = 12; type = "J"; break;
				case "JAL": opCode = 13; type = "J"; break;
				case "LI":  opCode = 14; type = "J"; break;
				}
				
				if(cond != null){
					switch (cond){
					case "AL": condValue = 0; break;
					case "NV": condValue = 1; break;
					case "EQ": condValue = 2; break;
					case "NE": condValue = 3; break;
					case "VS": condValue = 4; break;
					case "VC": condValue = 5; break;
					case "MI": condValue = 6; break;
					case "PL": condValue = 7; break;
					case "CS": condValue = 8; break;
					case "CC": condValue = 9; break;
					case "HI": condValue = 10; break;
					case "LS": condValue = 11; break;
					case "GT": condValue = 12; break;
					case "LT": condValue = 13; break;
					case "GE": condValue = 14; break;
					case "LE": condValue = 15; break;
					}
				}
				Integer temp = 0;
				switch (type) {
				case "R":
					//OpCode(4)Cond(4)S(1)opx(3)regD(4)regS(4)regT(4)
					result[5] = instruction3;
					result[4] = instruction2;
					result[3] = instruction1;
					temp = opx + 8 * setBit;
					result[2] = Integer.toHexString(temp);
					result[1] = Integer.toHexString(condValue);
					result[0] = Integer.toHexString(opCode);
					break;
				case "D":
					//OpCode(4)Cond(4)S(1)opx(7)regS(4)regT(4)
					//EX: LW 2,-4,C Loads C+-4 into 2
					result[5] = instruction1;
					result[4] = instruction3;
					temp = Integer.parseInt(instruction2, 16) + 128 * setBit;
					result[3] = Integer.toString(temp).substring(1);
					result[2] = Integer.toString(temp).substring(0,1);
					result[1] = condValue.toString();
					result[0] = opCode.toString();
					break;
				case "B":
					//OpCode(4)Cond(4)Label(16)
					temp = Integer.parseInt(instruction1, 16);
					result[5] = Integer.toString(temp).substring(3,4);
					result[4] = Integer.toString(temp).substring(2,3);
					result[3] = Integer.toString(temp).substring(1,2);
					result[2] = Integer.toString(temp).substring(0,1);
					result[1] = condValue.toString();
					result[0] = opCode.toString();
					break;
				case "J":
					//OpCode(4)Constant(20)
					temp = Integer.parseInt(instruction1, 16);
					result[5] = Integer.toString(temp).substring(4,5);
					result[4] = Integer.toString(temp).substring(3,4);
					result[3] = Integer.toString(temp).substring(2,3);
					result[2] = Integer.toString(temp).substring(1,2);
					result[1] = Integer.toString(temp).substring(0,1);
					result[0] = opCode.toString();
					break;
				}
				
				String output = count.toString() + "  :  " ;
				for(String s : result){
					output = output + s;
				}
				output = output + ";";
				
				//now write to the .mif file
				bw.write("output");
				bw.newLine();
				
				//increment the counter
				count++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			bw.write("END;");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}