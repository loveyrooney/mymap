package com.mymap.mymap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Array;
import java.util.*;


@SpringBootTest
public class Algorithm {
    @Test
    public void test(){
        int[] nums1 = new int[]{1,2,3,0,0,0};
        int[] nums2 = new int[]{2,5,6};
        int m = 3;
        int n = 3;
        Integer[] nums = Arrays.stream(nums1).boxed().toArray(Integer[]::new);
        Set<Integer> set = new LinkedHashSet<>(Arrays.asList(nums));
        System.out.println(set);

        Map<Character,Integer> map = new HashMap<>();
        map.put('I',1);
        Set<Character> chars = new HashSet<>(Arrays.asList('I'));
        System.out.println(map.containsKey('I'));
        System.out.println(chars.contains('I'));
        String h = "hello";
        h.stripLeading();
        System.out.println(h.stripLeading().length());

        lengthOfLastWord("hello   ");
        lengthOfLastWord2("hello   ");
    }

    public int lengthOfLastWord(String s) {
        System.out.println(System.currentTimeMillis());
        int answer = 0;
        for(int i=s.stripTrailing().length()-1; i>=0; i--){
            if(s.stripTrailing().charAt(i)==' '){
                break;
            } else {
                answer++;
            }
        }
        System.out.println(System.currentTimeMillis());
        return answer;
    }

    public int lengthOfLastWord2(String s) {
        System.out.println(System.currentTimeMillis());
        int answer = 0;
        for(int i=s.length()-1; i>=0; i--){
            if(s.charAt(i)==' ' && answer==0){
                break;
            }
            if(s.charAt(i)!=' '){
                answer++;
            }
        }
        System.out.println(System.currentTimeMillis());
        return answer;
    }
}
