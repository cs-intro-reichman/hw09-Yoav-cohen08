import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Random;

public class LanguageModel 
{
    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;

    // The window length used in this model.
    int windowLength;
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) 
    {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) 
    {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) 
    {
        String window = "";
        char c;
        In in = new In(fileName);
        for (int i = 0; i < windowLength; i++)
        { 
            char temp  = in.readChar();
            window += temp;
        }

        while (!in.isEmpty())
        {
            c = in.readChar();
            List probs = CharDataMap.get(window);
            if (probs == null) // Creates a new empty list, and adds (window,list) to the map
                {
                    probs = new List();
                    CharDataMap.put(window, probs);
                }
                probs.update(c);
                window += c;
                window = window.substring(1);
            }
        for(List probs : CharDataMap.values())
        {
            calculateProbabilities(probs);
        }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public static void calculateProbabilities(List probs) 
    {				
		double totalchars = 0;
        Node current = probs.getFirstN();
        while (current != null) 
        {
        totalchars += (double)current.cd.count; // Calculates how many chars are in the phrase.
        current = current.next;
        }
        current = probs.getFirstN();
        double flag = 0;
        while (current != null) 
        {
            current.cd.p = ((double)current.cd.count) / totalchars;
            current.cd.cp = current.cd.p + flag;
            flag = current.cd.cp;
            current = current.next;
        }
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) 
    {
        //randomGenerator = new Random();
        double r = randomGenerator.nextDouble(); // Generate a random number in [0, 1)
        Node current = probs.getFirstN();        // Start from the first node in the list
        while (current != null) 
        {
            if (current.cd.cp > r) 
            {
                return current.cd.chr; // Return the character of the CD.
            }
            current = current.next; 
        }
        return '_';
    }
    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
    //If the length of the initial text (prompt) provided by the user is less than the windowLength,
    //we cannot generate any text. In this case we return the initial text, and terminate. 
    // The text generation process stops when the length of the generated text equals the desired
    //text length, as specified by the user. 
	public String generate(String initialText, int textLength) 
    {
        if (initialText.length() < windowLength)
        {
            return initialText;
        }
        String window = initialText.substring(initialText.length() - windowLength);
        String generatedText = window;
        while (generatedText.length() < (textLength + windowLength)) 
        {
            List charList = CharDataMap.get(window);                                         // Get the list of possible characters that can follow the current window
            if (charList == null)  // If the window is not found in the map, stop the process
            {
                break;
            }
             // Use the getRandomChar method to select a random character based on the probabilities
            char nextChar = getRandomChar(charList);
            generatedText += nextChar;
            window = generatedText.substring(generatedText.length() - windowLength);
        }
        return generatedText;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() 
    {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) 
        {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) 
    {
		int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];

        LanguageModel LM;
        if(randomGeneration)
        {
            LM = new LanguageModel(windowLength);
        }
        else
        {
            LM = new LanguageModel(windowLength, 20);
        }
        LM.train(fileName);
        System.out.println(LM.generate(initialText, generatedTextLength));
    }
}
