#include <stdio.h>

int main(){
    int i = 0;
    for(i = 0; i < 20; printf("(i=%d) ", i)){
        printf("i: %d\n", i);
        i++;
    }
    printf(">> i: %d\n", i);
}