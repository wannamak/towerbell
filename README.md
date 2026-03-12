# TowerBell

## About

TowerBell is Raspberry PI software which controls a relay to
ring a tower bell.

## Components

* Raspberry PI 4B
* DS3231 Precision RTC Breakout (a reliable clock)

## Setup notes

* As root, <code>timedatectl</code> to verify hwclock
* Config in <code>/boot/firmware/usercfg.txt</code>
```
# DS3231 RTC clock chip.
# https://askubuntu.com/questions/1260403/rapsberry-pi-4-with-rtc-and-ubuntu-20-04
dtoverlay=i2c-rtc,ds3231
```

## GPIO

* I unfortunately chose to upgrade the Pi to Ubuntu 24 LTS,
which removes the simple and easy-to-use `/sys/class/gpio`
interface in favor of the new `/dev/gpiochipN` interface.
