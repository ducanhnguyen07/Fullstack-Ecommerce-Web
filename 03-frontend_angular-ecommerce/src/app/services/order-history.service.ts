import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {OrderHistory} from "../common/order-history";

@Injectable({
  providedIn: 'root'
})
export class OrderHistoryService {

  private orderUrl = "http://192.168.42.113:30081/api/orders";
  constructor(private httpClient: HttpClient) { }


  getOrderHistory(theEmail: string): Observable<GetResponseOrderHistory>{

    //we need url based on the customer email
    const orderHistoryUrl = `${this.orderUrl}/search/findByCustomerEmail?email=${theEmail}`;


    return this.httpClient.get<GetResponseOrderHistory>(orderHistoryUrl);

  }

}

interface GetResponseOrderHistory{
  _embedded: {
    orders: OrderHistory[];
  }
}
