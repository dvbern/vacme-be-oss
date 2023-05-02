import {OdiUserJaxTS} from 'vacme-web-generated';

export default interface OdiUserPaging {
    list: Array<OdiUserJaxTS>;
    page: number;
    foundAll: boolean;
    odiIdentifier: string;
}
