package com.vlad.Model;

public class PCG32 {
    private long state;
    private final long inc;

    public PCG32(long seed, long streamId){
        this.state = 0;
        this.inc = (streamId << 1) | 1;
        nextInt();
        this.state += seed;
        nextInt();
    }

    public int nextInt(){
        long oldState = this.state;
        state = oldState * 6364136223846793005L + inc;

        int xorshifted = (int)(((oldState >> 18) ^ oldState) >> 27);
        int rot = (int)(oldState >> 59);

        return Integer.rotateRight(xorshifted, rot);
    }

    public int nextInt(int bound){
        if (bound <= 0)
            throw new IllegalArgumentException("Bound must be positive");

        int threshold = -bound % bound;

        while(true){
            int r = nextInt();

            if(r >= threshold)
                return r % bound;
        }
    }

    public double nextDouble(){
        long high = ((long) nextInt()) & 0xFFFFFFFFL;
        long low  = ((long) nextInt()) & 0xFFFFFFFFL;

        long combined = (high << 21) ^ (low >>> 11);

        return combined / (double)(1L << 53);
    }

}
