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

import {TSErrorLevel} from './TSErrorLevel';

import {TSErrorType} from './TSErrorType';

export default class TSExceptionReport {

    public type?: TSErrorType;
    public severity: TSErrorLevel;
    public msgKey?: string;
    public translatedMessage?: string;

    // fields for ExceptionReport entity
    public exceptionName?: string;
    public methodName?: string;
    public customMessage?: string;
    public errorCodeEnum?: string;
    public stackTrace?: string;
    public argumentList?: any;

    // fields for ViolationReports
    public path?: string;
    public constraintType?: string;



    /**
     *
     * @param options has multiple properties, i.e.
     * - type Type of the Error
     * - severity Severity of the Error
     * - msgKey This is the message key of the error. Should be translated on client
     * - translatedMessage This is the translated message from the server, no translation needed
     * - args This is a List or an element that gives more information about the offending value
     */
    constructor(options: {
        type?: TSErrorType;
        severity?: TSErrorLevel;
        msgKey?: string;
        translatedMessage?: string;
        path?: string;
        constraintType?: string;
        args?: any;
    } = {}) {
        this.type = options.type;
        this.severity = options.severity || TSErrorLevel.SEVERE;
        this.msgKey = options.msgKey;
        this.translatedMessage = options.translatedMessage;
        this.argumentList = options.args || [];
        this.path = options.path;
        this.constraintType = options.constraintType;
    }


    public static createFromViolation(key: string,  translatedMessage: string, path: string, value: string): TSExceptionReport {
        return new TSExceptionReport({
            type: TSErrorType.VALIDATION,
            severity: TSErrorLevel.SEVERE,
            msgKey: key,
            translatedMessage,
            path,
            args: [value],


        });

    }

    public static createClientSideError(severity: TSErrorLevel, msgKey: string, args: any): TSExceptionReport {
        return new TSExceptionReport({
            type: TSErrorType.CLIENT_SIDE,
            severity,
            msgKey,
            args,
        });
    }

    /**
     * takes a data Object that matches the fields of a EbeguExceptionReport and transforms them to a TSExceptionReport.
     *
     * @param data data for exception report
     * @returns  object that can be handled and displayd by error handling mechanism
     */
    public static createFromExceptionReport(data: any): TSExceptionReport {
        const msgToDisp = data.translatedMessage || data.customMessage || 'ERROR_UNEXPECTED_EBEGU_RUNTIME';
        const exceptionReport = new TSExceptionReport({
            type: TSErrorType.BADREQUEST,
            severity: TSErrorLevel.SEVERE,
            translatedMessage: msgToDisp,
            args: data.argumentList,
        });
        exceptionReport.errorCodeEnum = data.errorCodeEnum;
        exceptionReport.exceptionName = data.exceptionName;
        exceptionReport.methodName = data.methodName;
        exceptionReport.stackTrace = data.stackTrace;
        exceptionReport.translatedMessage = msgToDisp;
        exceptionReport.customMessage = data.customMessage;
        exceptionReport.argumentList = data.argumentList;

        return exceptionReport;
    }

    public isConstantValue(constant: any, value: any): boolean {
        const keys = Object.keys(constant);
        for (const key of keys) {
            if (value === constant[key]) {
                return true;
            }
        }

        return false;
    }

    public isValid(): boolean {
        const validType = this.isConstantValue(TSErrorType, this.type);
        const validSeverity = this.isConstantValue(TSErrorLevel, this.severity);
        const validMsgKey = typeof this.msgKey === 'string' && this.msgKey.length > 0;
        const validTranslatedMsg = typeof this.translatedMessage === 'string' && this.translatedMessage.length > 0;

        const allValid = validType && validSeverity && (validMsgKey || validTranslatedMsg);
        if (!allValid) {
            console.warn('invalid TSExceptionReport passed ', {
                validType,
                validSeverity,
                validMsgKey,
                validTranslatedMsg,
            });
        }

        return allValid;
    }

    public isInternal(): boolean {
        return this.type === TSErrorType.INTERNAL;
    }

}
