package com.focusstart.test;


import java.io.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        int argIndex = 0;
        Boolean isDecreasing = false;
        Boolean isInteger = null;
        String outputPath = null;
        ArrayList<String> inputNames = new ArrayList<String>();
        boolean isParamsCorrect = true;
        try {
            if (argIndex >= args.length) {
                isParamsCorrect = false;
            }
            if (args[argIndex].equals("-a")) {
                isDecreasing = false;
                argIndex += 1;
            } else if (args[argIndex].equals("-d")) {
                isDecreasing = true;
                argIndex += 1;
            }
            if (argIndex >= args.length) {
                isParamsCorrect = false;
            }
            if (args[argIndex].equals("-s")) {
                isInteger = false;
            } else if (args[argIndex].equals("-i")) {
                isInteger = true;
            } else {
                isParamsCorrect = false;
                System.out.println("no input type specified.");  // Exception for the not correct arg
            }
            argIndex += 1;
            if (argIndex >= args.length) {
                isParamsCorrect = false;
            }
            outputPath = args[argIndex];
            argIndex += 1;
            if (argIndex >= args.length) {
                isParamsCorrect = false;
            }
            for (int i = argIndex; i < args.length; i++) {
                inputNames.add(args[i]);
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("some params are missing"); //Exception for the missing param
        }

        if(isParamsCorrect) {
            ArrayList<Scanner> inputScanners = new ArrayList<Scanner>();
            for (String input : inputNames) {
                File file = new File(input);
                try {
                    Scanner scanner = new Scanner(file);
                    inputScanners.add(scanner);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    System.out.println("file " + input + " not found, skipping it.");  //Exception for the not find file
                }
            }


            ArrayList<Optional<Comparable>> currentElements = new ArrayList<Optional<Comparable>>();
            for (int i = 0; i < inputScanners.size(); i++) {
                currentElements.add(Optional.empty());
            }
            try {
                FileWriter writer = new FileWriter(outputPath, false);
                BufferedWriter bw = new BufferedWriter(writer);
                Optional<Comparable> last = Optional.empty();

                boolean flag = true;
                while (flag) {
                    fillCurrentElements(currentElements, inputScanners, isInteger);
                    if (isAllElementsEmpty(currentElements)) {
                        flag = false;
                    } else {
                        Optional<Comparable> next = getNext(currentElements, last, isDecreasing);
                        if (next.isPresent()) {
                            last = next;
                            bw.write(last.get() + "\n");
                        }
                    }
                }
                bw.close();
                writer.close();
            } catch (IOException e) {
                System.out.println("cannot write to file " + outputPath + " please specify proper output."); //Exception for the not correct output file
            }
        }
    }

    private static boolean isAllElementsEmpty(ArrayList<Optional<Comparable>> elems) {
        boolean result = true;
        for (Optional<Comparable> elem : elems) {
            if (elem.isPresent()) {
                result = false;
                break;
            }
        }
        return result;
    }

    private static void fillCurrentElements(
            ArrayList<Optional<Comparable>> elems,
            ArrayList<Scanner> inputs,
            boolean isInt
    ) {
        for (int i = 0; i < elems.size(); i++) {
            if (!elems.get(i).isPresent()) {
                Scanner currentInput = inputs.get(i);
                if (isInt && currentInput.hasNextInt()) {
                    Integer next = currentInput.nextInt();
                    elems.set(i, Optional.of(next));
                } else if (!isInt && currentInput.hasNext()) {
                    String next = currentInput.next();
                    elems.set(i, Optional.of(next));
                } else if (currentInput.hasNext()) {
                    System.out.println("input number " + i + " have something else than int. Skipping this"); //Exception if element not int
                    currentInput.next();
                    i = i - 1;
                } else {
                    //current input is empty
                }
            }
        }
    }


    private static Optional<Comparable> getNext(
            ArrayList<Optional<Comparable>> elems,
            Optional<Comparable> last,
            boolean isDecreasing
    ) {
        Optional<Comparable> minMax = Optional.empty();
        Integer minMaxIndex = null;

        for (int i = 0; i < elems.size(); i++) {
            if (!minMax.isPresent() && elems.get(i).isPresent()) {
                //if current minmax element is not defined and elems[i] is present, then we should write current elem to minmax
                minMax = elems.get(i);
                minMaxIndex = i;
            } else if (minMax.isPresent() && elems.get(i).isPresent()) {
                //if current minmax element and element[i] is present, then we should compare them
                Comparable currentMinMax = minMax.get();
                Comparable currentElem = elems.get(i).get();
                int res = currentElem.compareTo(currentMinMax);

                if (isDecreasing && res == 1) {
                    minMax = elems.get(i);
                    minMaxIndex = i;
                } else if (!isDecreasing && res == -1) {
                    minMax = elems.get(i);
                    minMaxIndex = i;
                }
            } else {
                //current elem[i] is not present, do nothing
            }
        }

        if (last.isPresent() && minMax.isPresent()) {
            //We have find current minMax element and got previous element, then we should compare them to check is ordering
            //still works as planed.
            Comparable currentMinMax = minMax.get();
            Comparable currentLast = last.get();
            int res = currentMinMax.compareTo(currentLast);

            if (res == -1 && isDecreasing || res == 1 && !isDecreasing || res == 0) {
                elems.set(minMaxIndex, Optional.empty());
                return minMax;
            } else {
                System.out.println("current element: " + currentMinMax + " current last element: "
                        + currentLast + " order is decreasing:" + isDecreasing + ", so we skip this element");
                elems.set(minMaxIndex, Optional.empty());
                return Optional.empty();
            }
        } else if (!last.isPresent() && minMax.isPresent()) {
            //We work with first element, so we should just return it.
            elems.set(minMaxIndex, Optional.empty());
            return minMax;
        } else return Optional.empty();
    }
}
