/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
#include "towerbell_pi_physical_GPIOController.h"

#include <cstring>
#include <string>

#include <errno.h>
#include <fcntl.h>
#include <gpiod.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <linux/gpio.h>

struct towerbell_gpio_context {
  struct gpiod_chip *chip;
  struct gpiod_line_request *line_request;
  int pin;
};

JNIEXPORT jlong JNICALL Java_towerbell_pi_physical_GPIOController_initializeOutput(
    JNIEnv *env, jobject obj, jstring chip_path, jint pin, jboolean is_active_low) {
  struct gpiod_chip *chip;
  const char* c_chip_path = env->GetStringUTFChars(chip_path, 0);
  chip = gpiod_chip_open(c_chip_path);
  if (!chip) {
    printf("open %s failed: %s\n", c_chip_path, strerror(errno));
    env->ReleaseStringUTFChars(chip_path, c_chip_path);
		return 0;
  }
  env->ReleaseStringUTFChars(chip_path, c_chip_path);

  struct gpiod_line_settings *settings = gpiod_line_settings_new();
  if (!settings) {
    printf("gpiod_line_settings_new failed: %s\n", strerror(errno));
    gpiod_chip_close(chip);
    return 0;
  }

  gpiod_line_settings_set_direction(settings, GPIOD_LINE_DIRECTION_OUTPUT);
  gpiod_line_settings_set_active_low(settings, is_active_low);
  gpiod_line_settings_set_output_value(settings, GPIOD_LINE_VALUE_INACTIVE);

  struct gpiod_line_config *line_cfg = gpiod_line_config_new();
  if (!line_cfg) {
    printf("gpiod_line_config_new failed: %s\n", strerror(errno));
    gpiod_line_settings_free(settings);
    gpiod_chip_close(chip);
    return 0;
  }

  if (gpiod_line_config_add_line_settings(line_cfg, (unsigned int*) &pin, 1, settings)) {
    printf("gpiod_line_config_add_line_settings failed: %s\n", strerror(errno));
    gpiod_line_config_free(line_cfg);
    gpiod_line_settings_free(settings);
    gpiod_chip_close(chip);
    return 0;
  }

  struct gpiod_request_config *req_cfg = gpiod_request_config_new();
  if (!req_cfg) {
    printf("gpiod_request_config_new failed: %s\n", strerror(errno));
    gpiod_line_config_free(line_cfg);
    gpiod_line_settings_free(settings);
    gpiod_chip_close(chip);
    return 0;
  }

  struct gpiod_line_request *line_request =
      gpiod_chip_request_lines(chip, req_cfg, line_cfg);
  if (!line_request) {
    printf("gpiod_chip_request_lines failed: %s\n", strerror(errno));
    gpiod_request_config_free(req_cfg);
    gpiod_line_config_free(line_cfg);
    gpiod_line_settings_free(settings);
    gpiod_chip_close(chip);
    return 0;
  }

  // This is never freed.
  struct towerbell_gpio_context *context = new towerbell_gpio_context;
  context->line_request = line_request;
  context->pin = pin;

  gpiod_request_config_free(req_cfg);
  gpiod_line_config_free(line_cfg);
  gpiod_line_settings_free(settings);
  gpiod_chip_close(chip);

  return (jlong) context;
}

JNIEXPORT jlong JNICALL Java_towerbell_pi_physical_GPIOController_initializeInput(
    JNIEnv *env, jobject obj, jstring chip_path, jint pin) {
  struct gpiod_chip *chip;
  const char* c_chip_path = env->GetStringUTFChars(chip_path, 0);
  chip = gpiod_chip_open(c_chip_path);
  if (!chip) {
    printf("open %s failed: %s\n", c_chip_path, strerror(errno));
    env->ReleaseStringUTFChars(chip_path, c_chip_path);
		return 0;
  }
  env->ReleaseStringUTFChars(chip_path, c_chip_path);

  struct gpiod_line_settings *settings = gpiod_line_settings_new();
  if (!settings) {
    printf("gpiod_line_settings_new failed: %s\n", strerror(errno));
    gpiod_chip_close(chip);
    return 0;
  }

  gpiod_line_settings_set_direction(settings, GPIOD_LINE_DIRECTION_INPUT);

  struct gpiod_line_config *line_cfg = gpiod_line_config_new();
  if (!line_cfg) {
    printf("gpiod_line_config_new failed: %s\n", strerror(errno));
    gpiod_line_settings_free(settings);
    gpiod_chip_close(chip);
    return 0;
  }

  if (gpiod_line_config_add_line_settings(line_cfg, (unsigned int*)&pin, 1, settings)) {
    printf("gpiod_line_config_add_line_settings failed: %s\n", strerror(errno));
    gpiod_line_config_free(line_cfg);
    gpiod_line_settings_free(settings);
    gpiod_chip_close(chip);
    return 0;
  }

  struct gpiod_request_config *req_cfg = gpiod_request_config_new();
  if (!req_cfg) {
    printf("gpiod_request_config_new failed: %s\n", strerror(errno));
    gpiod_line_config_free(line_cfg);
    gpiod_line_settings_free(settings);
    gpiod_chip_close(chip);
    return 0;
  }

  struct gpiod_line_request *line_request =
      gpiod_chip_request_lines(chip, req_cfg, line_cfg);
  if (!line_request) {
    printf("gpiod_chip_request_lines failed: %s\n", strerror(errno));
    gpiod_request_config_free(req_cfg);
    gpiod_line_config_free(line_cfg);
    gpiod_line_settings_free(settings);
    gpiod_chip_close(chip);
    return 0;
  }

  // This is never freed.
  struct towerbell_gpio_context *context = new towerbell_gpio_context;
  context->line_request = line_request;
  context->pin = pin;

  gpiod_request_config_free(req_cfg);
  gpiod_line_config_free(line_cfg);
  gpiod_line_settings_free(settings);
  gpiod_chip_close(chip);

  return (jlong) context;
}

JNIEXPORT jint JNICALL Java_towerbell_pi_physical_GPIOController_setInternal(
    JNIEnv *env, jobject obj, jlong context_ptr, jboolean is_active) {
  struct towerbell_gpio_context *context = (struct towerbell_gpio_context *) context_ptr;

  if (gpiod_line_request_set_value(context->line_request, context->pin,
      is_active ? GPIOD_LINE_VALUE_ACTIVE : GPIOD_LINE_VALUE_INACTIVE)) {
    printf("gpiod_line_request_set_value failed: %s\n", strerror(errno));
    return -1;
  }

  return 0;
}

JNIEXPORT jboolean JNICALL Java_towerbell_pi_physical_GPIOController_getInternal(
    JNIEnv *env, jobject obj, jlong context_ptr) {
  struct towerbell_gpio_context *context = (struct towerbell_gpio_context *) context_ptr;

  enum gpiod_line_value value = gpiod_line_request_get_value(
      context->line_request, context->pin);

  return value == GPIOD_LINE_VALUE_ACTIVE;
}
