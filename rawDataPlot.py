#!/usr/bin/python

import numpy as np
import matplotlib.pyplot as plt

data1 = np.loadtxt('rawDataY1.txt')
data2 = np.loadtxt('rawDataY2.txt')

data3 = np.loadtxt('rawDataZ1.txt')
data4 = np.loadtxt('rawDataZ2.txt')


data1a = np.array(data1)
data2a = np.array(data2)

data3a = np.array(data3)
data4a = np.array(data4)

def f(t):
    return np.exp(-t) * np.cos(2*np.pi*t)

t1 = np.arange(0.0, 5.0, 0.1)
t2 = np.arange(0.0, 5.0, 0.02)


plt.figure(1)
plt.subplot(211)
#plt.plot(t1, f(t1), 'bo', t2, f(t2), 'k')
plt.plot(data1a, 'b--', data2a, 'r--')

plt.subplot(212)
#plt.plot(t2, np.cos(2*np.pi*t2), 'r--')

plt.plot(data3a, 'b--', data4a, 'r--')

plt.show()
