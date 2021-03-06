import router from "@/router";
import {GameObject} from "@/game/GameObject";
import {MapData} from "@/game/Grid";
import {Attr} from "@/net/Packets";


export default class Client {

    public static instance: Client;

    /**
     * url для открытия websocket коннекта
     */
    public static wsUrl: string;

    /**
     * url для работы с API методами бекенда (авторизация, регистрация и тд)
     */
    public static apiUrl: string;

    /**
     * ид текущей сессии используемый для запросов серверу
     */
    public ssid?: string;

    /**
     * была ли хоть одна попытка входа?
     */
    public wasLoginTry: boolean = false;

    /**
     * надо ли сделать автоматическую попытку входа при загрузке логин экрана
     */
    public needAutologin: boolean = false;

    /**
     * последняя ошибка зарегистрированная
     */
    public lastError?: string = undefined

    /**
     * данные карты (тайлы)
     */
    public map: { [key: string]: MapData } = {};

    /**
     * игровые объекты полученные с сервера
     */
    public objects: { [key: number]: GameObject } = {}

    /**
     * статус моего персонажа
     */
    public myStatus: number[] = []

    public chatHistory: string[] = []
    public onChatMessage ?: Callback

    public get playerObject(): GameObject {
        return this.objects[this.selectedCharacterId]
    }

    /**
     * ид выбранного персонажа
     */
    public get selectedCharacterId(): number {
        return parseInt(localStorage.getItem("selectedCharacterId") || "0");
    }

    public set selectedCharacterId(v: number) {
        localStorage.setItem("selectedCharacterId", v.toString())
    }

    constructor() {
        this.ssid = localStorage.getItem("ssid") || undefined;
    }

    public isLogged(): boolean {
        return this.ssid !== undefined;
    }

    /**
     * сетевая ошибка
     * @param e сообщение об ошибке
     */
    public networkError(e: string) {
        console.warn("networkError " + e)
        this.lastError = e;
        this.ssid = undefined
        router.push({name: 'Login'});
    }

    /**
     * произошел успешный вход через API
     * @param ssid
     */
    public sucessLogin(ssid: string) {
        Client.instance.ssid = ssid;
        localStorage.setItem("ssid", ssid);
        router.push({name: "Characters"});
    }

    /**
     * разлогинится и вернуться на экран входа
     */
    public logout() {
        Client.instance.ssid = undefined;
        localStorage.removeItem("ssid");
        router.push({name: "Login"})
    }

    /**
     * очистить игровые данные
     */
    public clear() {
        this.map = {};
        this.objects = {}
        this.chatHistory = []
    }
}