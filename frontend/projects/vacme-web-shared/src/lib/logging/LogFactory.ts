/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {Empty} from '../types';
import {parseColor} from '../util/parseColor';
import {isDark, isSeverityEnabled, parseSeverity} from './logging-helpers';
import {LogSeverity} from './LogSeverity';

/* eslint-disable no-console */
export class LogFactory {
  private static traceEnabledFor: string[] | Empty;
  public static minSeverity: LogSeverity = LogSeverity.INFO;

  public static init(logSeverity: LogSeverity | any, traceEnabledFor: string[] | Empty): void {
    LogFactory.minSeverity = parseSeverity(logSeverity);
    LogFactory.traceEnabledFor = traceEnabledFor;
  }

  constructor() {
    window.onerror = (msg): void => {
      alert('Unhandled internal error: ' + msg);
      // noinspection EmptyCatchBlockJS
      try {
        if (console.log) {
          console.log('Unhandled internal error', msg);
        }
      } catch (ignored) {
        // probably IE with a closed console
      }
    };
  }

  /**
   * Usage: const LOG = LogFactory.createLog("MyFooCompoment");
   */
  public static createLog(loggerName: string): Log {
    const bgColor = '#' + this.getHashColorCode(loggerName);
    const fgColor = isDark(parseColor(bgColor))
      ? 'white'
      : 'black';
    const loggerEnabled = (name: string, severity: LogSeverity): boolean => this.isLoggerEnabled(name, severity);

    return new Log(
      loggerName,
      fgColor,
      bgColor,
      loggerEnabled,
    );
  }

  private static getHashColorCode(name: string): string {
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      // eslint-disable-next-line no-bitwise
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    const c: string = Math.abs(hash)
      .toString(16)
      .toUpperCase();

    if (c.length > 6) {
      return c.substring(0, 6);
    }
    return c.padStart(6, '0');
  }

  private static isLoggerEnabled(loggerName: string, severity: LogSeverity): boolean {
    return this.isTraceEnabled(loggerName) || this.isSeverityEnabled(severity);
  }

  private static isSeverityEnabled(severity: LogSeverity): boolean {
    return isSeverityEnabled(severity, LogFactory.minSeverity);
  }

  private static isTraceEnabled(loggerName: string): boolean {
    return (this.traceEnabledFor?.includes('*')
      || this.traceEnabledFor?.includes(loggerName))
      ?? false;
  }

}

export class Log {
  constructor(
    private readonly name: string,
    private readonly fgColor: string,
    private readonly bgColor: string,
    private readonly loggerEnabled: (loggerName: string, severity: LogSeverity) => boolean,
  ) {
    // nop
  }

  public info(message: any, ...args: any[]): void {
    this.doApply(console.info, LogSeverity.INFO, message, args);
  }

  public warn(message: any, ...args: any[]): void {
    this.doApply(console.warn, LogSeverity.WARN, message, args);
  }

  public error(message: any, ...args: any[]): void {
    this.doApply(console.error, LogSeverity.ERROR, message, args);
  }

  public debug(message: any, ...args: any[]): void {
    this.doApply(console.debug, LogSeverity.DEBUG, message, args);
  }

  /**
   * Trace must be explicitly enabled in environment.ts for specific loggers.
   */
  public trace(message: any, ...args: any[]): void {
    this.doApply(console.debug, LogSeverity.TRACE, message, args);
  }

  private doApply(
    logFunc: (message?: any, ...optionalParams: any[]) => any,
    severity: LogSeverity,
    message: string,
    args: any,
  ): void {
    if (!this.loggerEnabled(this.name, severity)) {
      return;
    }

    const callee = logFunc || console.log;
    if (!callee) {
      return;
    }

    let params = [];
    params.push(`%c${this.name}`);
    params.push(`color: ${this.fgColor}; background-color: ${this.bgColor}`);
    params.push(severity.toString());
    params.push(message);
    params = params.concat(args);

    // noinspection EmptyCatchBlockJS
    try {
      callee.call(null, ...params);
    } catch (ignored) {
      // probably IE with a closed console
    }
  }
}


