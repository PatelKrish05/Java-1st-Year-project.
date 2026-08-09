import java.util.*;

class Stack
{
    static int top;
    int cap;
    String temp[];

    Stack(int max){
        cap = max;
        top = -1;
    }

    void push(String tic, String[] ft)
    {
        top++;
        ft[top] = tic;
       /* System.out.println("element enter successfully");*/
    }

    String popRemove(String[] a)
    {
        return a[top--];
    }

    void popBook(String tic)
    {
        top--;
    }

    /*void display()
    {
        for (int i = top; i >= 0; i--)
        {
            System.out.println(a[i]);
        }
    }*/
}