package KMPAlgorithm;

public class KMP {
    private int[] Lps;
    private String pattern;

    public KMP() {
    }

    public Boolean accept(String pattern, String str) {
        if (pattern == null || pattern.isEmpty() || str == null || str.isEmpty()) {
            return false;
        }
        Boolean accept = false;
        this.pattern = pattern;
        this.Lps = new int[pattern.length()];
        get_Lps();
        int j = 0;
        for (int i = 0; i < str.length(); ) {
            if (pattern.charAt(j) == str.charAt(i)) {
                j++;
                i++;
            }

            if (j == pattern.length()) {
                accept = true;
                j = this.Lps[j - 1];
            }

            else if (i < str.length() && pattern.charAt(j) != str.charAt(i)) {
                if (j == 0) {
                    i++;
                } else {
                    j = this.Lps[j - 1];
                }
            }
        }
        return accept;
    }

    private void get_Lps() {
        int len = 0;
        int i = 1;
        this.Lps[0] = 0; 

        while (i < this.pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                this.Lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = this.Lps[len - 1];
                } else {
                    this.Lps[i] = len;
                    i++;
                }
            }
        }
    }
}