package org.ethereum.util;

import org.ethereum.facade.Repository;

import java.math.BigInteger;

public class BIUtil {


    /**
     * @param value - not null
     * @return true - if the param is zero
     */
    public static boolean isZero(BigInteger value){
        return value.compareTo(BigInteger.ZERO) == 0;
    }

    /**
     * @param valueA - not null
     * @param valueB - not null
     * @return true - if the valueA is equal to valueB is zero
     */
    public static boolean isEqual(BigInteger valueA, BigInteger valueB){
        return valueA.compareTo(valueB) == 0;
    }

    /**
     * @param valueA - not null
     * @param valueB - not null
     * @return true - if the valueA is not equal to valueB is zero
     */
    public static boolean isNotEqual(BigInteger valueA, BigInteger valueB){
        return !isEqual(valueA, valueB);
    }

    /**
     * @param valueA - not null
     * @param valueB - not null
     * @return true - if the valueA is less than valueB is zero
     */
    public static boolean isLessThan(BigInteger valueA, BigInteger valueB){
        return valueA.compareTo(valueB) == -1;
    }

    /**
     * @param valueA - not null
     * @param valueB - not null
     * @return true - if the valueA is more than valueB is zero
     */
    public static boolean isMoreThan(BigInteger valueA, BigInteger valueB){
        return valueA.compareTo(valueB) == 1;
    }


    /**
     * @param valueA - not null
     * @param valueB - not null
     * @return sum - valueA + valueB
     */
    public static BigInteger sum(BigInteger valueA, BigInteger valueB){
        return valueA.add(valueB);
    }


    /**
     * @param data = not null
     * @return new positive BigInteger
     */
    public static BigInteger toBI(byte[] data){
        return new BigInteger(1, data);
    }

    /**
     * @param data = not null
     * @return new positive BigInteger
     */
    public static BigInteger toBI(long data){
        return BigInteger.valueOf(data);
    }


    public static boolean isPositive(BigInteger value){
        return value.signum() > 0;
    }

    public static boolean isCovers(BigInteger covers, BigInteger value){
        return covers.compareTo(value) > -1;
    }

    public static boolean isNotCovers(BigInteger covers, BigInteger value){
        return !(covers.compareTo(value) > -1);
    }


    public static void transfer(Repository repository, byte[] fromAddr, byte[] toAddr, BigInteger value){
        repository.addBalance(fromAddr, value.negate());
        repository.addBalance(toAddr, value);
    }

    public static boolean exitLong(BigInteger value){

        return (value.compareTo(new BigInteger(Long.MAX_VALUE + ""))) > -1;
    }
}
