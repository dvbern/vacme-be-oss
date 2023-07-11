interface HasId {
    id?: string;
}

export default class UniqueElementUtil {

    public static uniqueObjectsById<T extends HasId>(listToCheck: Array<T>): Array<T> {
        const setOfIds = new Set(listToCheck.map(a => a.id));
        const uniqueObjects: Array<T> = Array.from(setOfIds)
            .map(id => {
                return listToCheck.find(elem => elem.id === id);
            })
            .filter(value => {
                return value !== undefined;
                // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            }).map(ele => ele!);
        return uniqueObjects;

    }
}
