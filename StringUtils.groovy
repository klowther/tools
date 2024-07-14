/**
 * ***********************************************************************************
 * UNCLASSIFIED - A collection of string utilities
 * @author D99510
 * *********************************************************************************** 
 * **/
public class StringUtils {
    /**
     * ***********************************************************************************
     * UNCLASSIFIED - determines if a string is quoted
     * @return - TRUE if string is quoted FALSE otherwise
     * @author D99510
     * *********************************************************************************** 
     * **/
    public static boolean quoted(String s) {
        if (s.length() < 2)
            return false;
        
        if (s.charAt(0) != '"')
            return false;
        
        if (s.charAt(s.length()-1) != '"')
            return false;
        
        return true;
    }
    
    /**
     * ***********************************************************************************
     * UNCLASSIFIED - returns a quoted version of the string
     * @return - the quoted string
     * @author D99510
     * *********************************************************************************** 
     * **/
    public static String quote(String s) {
        return '"' + s + '"';
    }
    
    /**
     * ***********************************************************************************
     * UNCLASSIFIED - returns a unquoted version of the string
     * @return - the unquoted string
     * @author D99510
     * *********************************************************************************** 
     * **/
    public static String unquote(String s) {
        if (quoted(s))
            return s.substring(1, s.length()-1);
        return (s);
    }    
}