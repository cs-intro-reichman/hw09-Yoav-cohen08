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
        In in = new In(fileName);
        while (in.hasNextChar()) 
        {
            if (window.length() >= windowLength)  // Remove the first character when the window is full
            {
            window = window.substring(1);
            }
            char c = in.readChar();
            window += c;
            if (window.length() == windowLength && in.hasNextChar()) 
            {
                // Get the next character after the window
                char nextChar = in.readChar();
                List probs = CharDataMap.get(window);
                if (probs == null) 
                {
                probs = new List();
                CharDataMap.put(window, probs);
                }
                probs.update(nextChar);
            }
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
        double cumulativeProbability = 0;
        while (current != null) 
        {
            current.cd.p = Math.round((current.cd.count / totalchars) * 100.0) / 100.0;
            cumulativeProbability += current.cd.p;
            current.cd.cp = Math.round(cumulativeProbability * 100.0) / 100.0;
            current = current.next;
        }
        //double flag = 0;
        /*while (current != null) 
        {
        current.cd.p = (double)current.cd.count / totalchars;
        current.cd.cp = flag + current.cd.p;
        flag = current.cd.cp;
        current = current.next;
        }
        */
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
        throw new NoSuchElementException("No character found for random number " + r);
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
        StringBuilder generatedText = new StringBuilder(initialText);
        while (generatedText.length() < textLength) 
        {
        String window = generatedText.substring(generatedText.length() - windowLength);  // Get the current window, which is the last 'windowLength' characters of the generated text
        List charList = CharDataMap.get(window);                                         // Get the list of possible characters that can follow the current window
        if (charList == null)  // If the window is not found in the map, stop the process
        {
            break;
            //return "The text that was generated so far.";
        }
        // Use the getRandomChar method to select a random character based on the probabilities
        char nextChar = getRandomChar(charList);
        generatedText.append(nextChar);
        }
        return generatedText.toString();
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
